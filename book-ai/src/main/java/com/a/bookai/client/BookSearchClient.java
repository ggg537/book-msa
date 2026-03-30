package com.a.bookai.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSearchClient {

    private final WebClient bookSearchWebClient;

    // 전체 도서 목록 조회
    public List<Map<String, Object>> getAllBooks() {
        try {
            return bookSearchWebClient.get()
                    .uri("/api/books/list/all")
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("도서 목록 조회 실패: {}", e.getMessage());
            return List.of();
        }
    }

    // 키워드로 도서 검색
    public List<Map<String, Object>> searchBooks(String keyword) {
        try {
            Map<String, Object> response = bookSearchWebClient.get()
                    .uri(uri -> uri
                            .path("/api/books/search/filter")
                            .queryParam("bookName", keyword)
                            .queryParam("size", 20)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || response.get("content") == null) {
                log.warn("도서 검색 결과 없음 - keyword: {}", keyword);
                return List.of();
            }

            return (List<Map<String, Object>>) response.get("content");

        } catch (Exception e) {
            log.error("도서 검색 실패 - keyword: {}, error: {}", keyword, e.getMessage());
            return List.of();
        }
    }
}