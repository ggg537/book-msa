package com.bookes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import co.elastic.clients.util.NamedValue;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ElasticsearchClient esClient;

    // Redis 키 상수
    private static final String POPULAR_KEY = "popular:keywords";
    private static final String RECENT_KEY  = "recent:keywords";
    private static final int    RECENT_MAX  = 10;

    // ES 인덱스
    private static final String LOG_INDEX = "search_log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 검색 로그 저장
     * Redis: 실시간 카운팅/최근검색어
     * ES: 영구 저장 (기간별 통계, 백업용)
     */
    public void saveLog(String keyword, int resultCount) {

        // Redis 저장 (실시간)
        redisTemplate.opsForZSet().incrementScore(POPULAR_KEY, keyword, 1);
        redisTemplate.opsForList().leftPush(RECENT_KEY, keyword);
        redisTemplate.opsForList().trim(RECENT_KEY, 0, RECENT_MAX - 1);

        // ES 저장 (영구 보관)
        try {
            Map<String, Object> logDoc = new HashMap<>();
            logDoc.put("keyword", keyword);
            logDoc.put("searchDate", LocalDateTime.now().format(FORMATTER));
            logDoc.put("resultCount", resultCount);

            esClient.index(i -> i
                    .index(LOG_INDEX)
                    .document(logDoc)
            );
        } catch (Exception e) {
            log.warn("ES 검색 로그 저장 실패 - keyword: {}, error: {}", keyword, e.getMessage());
        }

        log.info("검색 로그 저장 - keyword: {}, resultCount: {}", keyword, resultCount);
    }

    /**
     * 최근 검색어 TOP 10
     * Redis List에서 바로 조회 (빠름)
     */
    public List<String> getRecentKeywords() {
        List<String> keywords = redisTemplate.opsForList().range(RECENT_KEY, 0, RECENT_MAX - 1);
        if (keywords == null) return List.of();

        return keywords.stream()
                .distinct()
                .limit(RECENT_MAX)
                .toList();
    }

    /**
     * 인기 검색어 TOP 10
     * Redis ZSet에서 바로 조회 (빠름)
     */
    public List<String> getPopularKeywords() {
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(POPULAR_KEY, 0, RECENT_MAX - 1);
        if (keywords == null) return List.of();
        return keywords.stream().toList();
    }

    /**
     * 최근 검색어 전체 삭제
     * Redis + ES 둘 다 삭제
     */
    public void clearRecentKeywords() {
        // Redis 삭제 (즉시 반영)
        redisTemplate.delete(RECENT_KEY);

        // ES 삭제 (영구 데이터도 삭제)
        try {
            esClient.deleteByQuery(d -> d
                    .index(LOG_INDEX)
                    .query(q -> q.matchAll(m -> m))
                    .refresh(true)
            );
        } catch (Exception e) {
            log.warn("ES 검색 로그 삭제 실패 - error: {}", e.getMessage());
        }

        log.info("최근 검색어 전체 삭제 완료");
    }

    /**
     * 서버 재시작 시 ES → Redis 복원
     * ES에 저장된 데이터로 Redis 재구성
     */
    @PostConstruct
    public void restoreFromEs() {

        // 1. 인기 검색어 복원 (ZSet)
        try {
            SearchResponse<Void> response = esClient.search(s -> s
                            .index(LOG_INDEX)
                            .aggregations("popular_keywords", a -> a
                                    .terms(t -> t
                                            .field("keyword")
                                            .size(100)
                                    )
                            )
                            .size(0),
                    Void.class
            );

            response.aggregations()
                    .get("popular_keywords")
                    .sterms()
                    .buckets().array()
                    .forEach(bucket -> {
                        String keyword = bucket.key().stringValue();
                        long count = bucket.docCount();
                        redisTemplate.opsForZSet().add(POPULAR_KEY, keyword, count);
                    });

            log.info("ES → Redis 인기검색어 복원 완료");

        } catch (Exception e) {
            log.warn("ES → Redis 인기검색어 복원 실패 - error: {}", e.getMessage());
        }

        // 2. 최근 검색어 복원 (List)
        try {
            SearchResponse<Map> recentResponse = esClient.search(s -> s
                            .index(LOG_INDEX)
                            .sort(so -> so.field(f -> f
                                    .field("searchDate")
                                    .order(SortOrder.Desc)))
                            .size(RECENT_MAX),
                    Map.class
            );

            recentResponse.hits().hits().forEach(hit -> {
                if (hit.source() != null) {
                    String keyword = (String) hit.source().get("keyword");
                    if (keyword != null) {
                        redisTemplate.opsForList().rightPush(RECENT_KEY, keyword);
                    }
                }
            });

            log.info("ES → Redis 최근검색어 복원 완료");

        } catch (Exception e) {
            log.warn("ES → Redis 최근검색어 복원 실패 - error: {}", e.getMessage());
        }
    }
}