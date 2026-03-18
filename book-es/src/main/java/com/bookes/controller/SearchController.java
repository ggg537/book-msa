package com.bookes.controller;

import com.bookes.dto.BookSearchRequest;
import com.bookes.dto.BookSearchResponse;
import com.bookes.dto.PageResponse;
import com.bookes.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search/filter")
    public ResponseEntity<PageResponse<BookSearchResponse>> searchBooks(
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String authName,
            @RequestParam(required = false) String pubName,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String category,  // ✅ 추가
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {

        BookSearchRequest request = new BookSearchRequest(bookName, authName, pubName, minPrice, maxPrice, category);  // ✅ category 추가

        return ResponseEntity.ok(searchService.searchBooks(request, page, size));
    }

    // 자동완성 API
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "bookName") String field) throws Exception {
        return ResponseEntity.ok(searchService.autocomplete(keyword, field));
    }
}
