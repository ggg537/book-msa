package com.bookes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ElasticsearchClient esClient;
    private final ObjectMapper objectMapper;
    private final WebClient bookSearchWebClient;  // ✅ Bean 주입
    private static final String INDEX_NAME = "books";

    // ✅ @Value 삭제 (BookSearchWebClientConfig에서 이미 설정)

    /**
     * MySQL → ES 데이터 동기화
     * - Upsert: MySQL에 있는 데이터는 추가/업데이트
     * - Delete: MySQL에 없는데 ES에 있는 데이터는 삭제
     */
    public String syncBooks() throws Exception {

        // 1. MySQL 전체 도서 조회
        List<Map<String, Object>> books = bookSearchWebClient.get()  // ✅ 주입받은 WebClient 사용
                .uri("/api/books/list/all")  // ✅ baseUrl 이미 설정됨
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();

        if (books == null || books.isEmpty()) {
            return "동기화할 데이터가 없습니다.";
        }

        // 2. MySQL bookKey 목록 (Set으로 빠른 조회)
        Set<String> mysqlBookKeys = books.stream()
                .map(book -> String.valueOf(book.get("bookKey")))
                .collect(Collectors.toSet());

        // 3. ES에 현재 저장된 bookKey 목록 조회
        Set<String> esBookKeys = getEsBookKeys();

        // 4. ES에는 있는데 MySQL에는 없는 것 → 삭제 대상
        Set<String> deleteKeys = new HashSet<>(esBookKeys);
        deleteKeys.removeAll(mysqlBookKeys);

        // 5. Bulk 요청 (Upsert + Delete 함께 처리)
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

        // Upsert: MySQL 데이터 전부 추가/업데이트
        for (Map<String, Object> book : books) {
            String bookKey = String.valueOf(book.get("bookKey"));
            bulkBuilder.operations(op -> op
                    .index(i -> i
                            .index(INDEX_NAME)
                            .id(bookKey)
                            .document(book)
                    )
            );
        }

        // Delete: MySQL에서 삭제된 데이터 ES에서도 삭제
        for (String deleteKey : deleteKeys) {
            bulkBuilder.operations(op -> op
                    .delete(d -> d
                            .index(INDEX_NAME)
                            .id(deleteKey)
                    )
            );
        }

        BulkResponse response = esClient.bulk(bulkBuilder.build());

        if (response.errors()) {
            log.error("Bulk 처리 중 에러 발생");
            return "동기화 중 에러 발생";
        }

        log.info("동기화 완료 - Upsert: {}건, Delete: {}건", books.size(), deleteKeys.size());
        return String.format("동기화 완료 - Upsert: %d건, Delete: %d건", books.size(), deleteKeys.size());
    }

    /**
     * ES에 저장된 전체 bookKey 조회
     */
    private Set<String> getEsBookKeys() throws Exception {
        Set<String> bookKeys = new HashSet<>();

        SearchResponse<Map> response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q.matchAll(m -> m))
                        .source(src -> src.filter(f -> f.includes("bookKey")))
                        .size(10000),
                Map.class
        );

        for (Hit<Map> hit : response.hits().hits()) {
            if (hit.source() != null && hit.source().get("bookKey") != null) {
                bookKeys.add(String.valueOf(hit.source().get("bookKey")));
            }
        }

        if (bookKeys.size() >= 10000) {
            log.warn("ES 조회 결과 10000건 한계 도달. 누락 가능성 있음.");  // ✅ 경고 로그 추가
        }

        log.info("ES 현재 저장된 문서 수: {}건", bookKeys.size());
        return bookKeys;
    }
}
