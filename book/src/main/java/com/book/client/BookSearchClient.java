package com.book.client;

import com.book.dto.request.BookSearchRequest;
import com.book.dto.response.BookSearchResponse;
import com.book.dto.response.PageResponse;
import com.book.exception.BusinessException;
import com.book.exception.errorCode.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookSearchClient {

    private final RestClient bookEsRestClient;
    private final ObjectMapper objectMapper;

    // 검색어 자동 완성
    public List<String> autocomplete(String keyword, String field) {
        try {
            return bookEsRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/books/autocomplete")
                            .queryParam("keyword", keyword)
                            .queryParam("field", field)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 검색 + 필터
    public PageResponse<BookSearchResponse> searchWithFilter(BookSearchRequest request, int page, int size) {
        try {
            return bookEsRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/books/search/filter");

                        Map<String, Object> params = objectMapper.convertValue(request, new TypeReference<>() {});
                        params.forEach((key, value) -> {
                            if (value != null) uriBuilder.queryParam(key, value);
                        });

                        uriBuilder.queryParam("page", page);
                        uriBuilder.queryParam("size", size);
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<BookSearchResponse>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 검색 로그 ES에 저장
    public void saveSearchLog(String keyword, int resultCount) {
        try {
            bookEsRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/log/search")
                            .queryParam("keyword", keyword)
                            .queryParam("resultCount", resultCount)
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("검색 로그 저장 실패 - keyword: {}, error: {}", keyword, e.getMessage());
        }
    }

    // 최근 검색어 조회
    public List<String> getRecentKeywords() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/log/recent")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 인기 검색어 조회
    public List<String> getPopularKeywords() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/log/popular")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 최근 검색어 전체 삭제
    public void deleteAllRecentKeywords() {
        try {
            bookEsRestClient.delete()
                    .uri("/api/log/recent/all")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 일별 검색 트렌드
    public List<Map<String, Object>> getSearchTrend() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/stats/search-trend")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 월별 검색 트렌드
    public List<Map<String, Object>> getMonthlyTrend() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/stats/monthly-trend")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 인기 검색어 TOP 10
    public List<Map<String, Object>> getPopularKeywordStats() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/stats/popular-keywords")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }

    // 카테고리별 도서 수
    public List<Map<String, Object>> getCategoryCount() {
        try {
            return bookEsRestClient.get()
                    .uri("/api/stats/category-count")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SEARCH_SERVER_ERROR);
        }
    }
}
