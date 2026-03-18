package com.bookes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {

    private final ElasticsearchClient esClient;

    private static final String LOG_INDEX  = "search_log";
    private static final String BOOK_INDEX = "books";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 일별 검색 트렌드 (최근 30일)
     * searchDate 필드를 날짜별로 그룹핑 → 하루 검색 횟수
     */
    public List<Map<String, Object>> getSearchTrend() throws Exception {
        String since = LocalDateTime.now().minusDays(30).format(FORMATTER);

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(LOG_INDEX)
                        .query(q -> q
                                .range(r -> r
                                        .date(d -> d
                                                .field("searchDate")
                                                .gte(since)
                                        )
                                )
                        )
                        .aggregations("daily_trend", a -> a
                                .dateHistogram(dh -> dh
                                        .field("searchDate")
                                        .calendarInterval(CalendarInterval.Day)
                                        .format("yyyy-MM-dd")
                                )
                        )
                        .size(0),
                Void.class
        );

        List<Map<String, Object>> result = new ArrayList<>();
        response.aggregations()
                .get("daily_trend")
                .dateHistogram()
                .buckets().array()
                .forEach(bucket -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", bucket.keyAsString());
                    item.put("count", bucket.docCount());
                    result.add(item);
                });

        return result;
    }

    /**
     * 인기 검색어 TOP 10 (최근 30일)
     * keyword 필드로 그룹핑 → 검색 횟수 많은 순
     */
    public List<Map<String, Object>> getPopularKeywordStats() throws Exception {
        String since = LocalDateTime.now().minusDays(30).format(FORMATTER);

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(LOG_INDEX)
                        .query(q -> q
                                .range(r -> r
                                        .date(d -> d
                                                .field("searchDate")
                                                .gte(since)
                                        )
                                )
                        )
                        .aggregations("popular_keywords", a -> a
                                .terms(t -> t
                                        .field("keyword")
                                        .size(10)
                                )
                        )
                        .size(0),
                Void.class
        );

        List<Map<String, Object>> result = new ArrayList<>();
        response.aggregations()
                .get("popular_keywords")
                .sterms()
                .buckets().array()
                .forEach(bucket -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("keyword", bucket.key().stringValue());
                    item.put("count", bucket.docCount());
                    result.add(item);
                });

        return result;
    }

    /**
     * 카테고리별 도서 수
     * books 인덱스에서 category 필드로 그룹핑
     */
    public List<Map<String, Object>> getCategoryCount() throws Exception {

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(BOOK_INDEX)
                        .aggregations("category_count", a -> a
                                .terms(t -> t
                                        .field("category")
                                        .size(50)
                                )
                        )
                        .size(0),
                Void.class
        );

        List<Map<String, Object>> result = new ArrayList<>();
        response.aggregations()
                .get("category_count")
                .sterms()
                .buckets().array()
                .forEach(bucket -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("category", bucket.key().stringValue());
                    item.put("count", bucket.docCount());
                    result.add(item);
                });

        return result;
    }

    /**
     * 월별 검색 트렌드 (최근 12개월)
     */
    public List<Map<String, Object>> getMonthlySearchTrend() throws Exception {
        String since = LocalDateTime.now().minusMonths(12).format(FORMATTER);

        SearchResponse<Void> response = esClient.search(s -> s
                        .index(LOG_INDEX)
                        .query(q -> q
                                .range(r -> r
                                        .date(d -> d
                                                .field("searchDate")
                                                .gte(since)
                                        )
                                )
                        )
                        .aggregations("monthly_trend", a -> a
                                .dateHistogram(dh -> dh
                                        .field("searchDate")
                                        .calendarInterval(CalendarInterval.Month)
                                        .format("yyyy-MM")
                                )
                        )
                        .size(0),
                Void.class
        );

        List<Map<String, Object>> result = new ArrayList<>();
        response.aggregations()
                .get("monthly_trend")
                .dateHistogram()
                .buckets().array()
                .forEach(bucket -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("month", bucket.keyAsString());
                    item.put("count", bucket.docCount());
                    result.add(item);
                });

        return result;
    }
}
