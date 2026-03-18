package com.book.controller;

import com.book.client.BookSearchClient;
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

    private final BookSearchClient bookSearchClient;

    @GetMapping("/search-trend")
    public ResponseEntity<List<Map<String, Object>>> getSearchTrend() {
        return ResponseEntity.ok(bookSearchClient.getSearchTrend());
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTrend() {
        return ResponseEntity.ok(bookSearchClient.getMonthlyTrend());
    }

    @GetMapping("/popular-keywords")
    public ResponseEntity<List<Map<String, Object>>> getPopularKeywords() {
        return ResponseEntity.ok(bookSearchClient.getPopularKeywordStats());
    }

    @GetMapping("/category-count")
    public ResponseEntity<List<Map<String, Object>>> getCategoryCount() {
        return ResponseEntity.ok(bookSearchClient.getCategoryCount());
    }
}
