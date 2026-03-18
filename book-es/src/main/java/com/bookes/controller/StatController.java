package com.bookes.controller;

import com.bookes.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    /**
     * 일별 검색 트렌드 (최근 30일)
     */
    @GetMapping("/search-trend")
    public ResponseEntity<List<Map<String, Object>>> getSearchTrend() throws Exception {
        return ResponseEntity.ok(statService.getSearchTrend());
    }

    /**
     * 월별 검색 트렌드 (최근 12개월)
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend() throws Exception {
        return ResponseEntity.ok(statService.getMonthlySearchTrend());
    }

    /**
     * 인기 검색어 TOP 10 (최근 30일)
     */
    @GetMapping("/popular-keywords")
    public ResponseEntity<List<Map<String, Object>>> getPopularKeywords() throws Exception {
        return ResponseEntity.ok(statService.getPopularKeywordStats());
    }

    /**
     * 카테고리별 도서 수
     */
    @GetMapping("/category-count")
    public ResponseEntity<List<Map<String, Object>>> getCategoryCount() throws Exception {
        return ResponseEntity.ok(statService.getCategoryCount());
    }
}
