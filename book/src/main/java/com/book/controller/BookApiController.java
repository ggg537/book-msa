package com.book.controller;

import com.book.client.BookSearchClient;
import com.book.dto.request.BookCreateRequest;
import com.book.dto.request.BookSearchRequest;
import com.book.dto.request.BookUpdateRequest;
import com.book.dto.response.BookResponse;
import com.book.dto.response.BookSearchResponse;
import com.book.dto.response.PageResponse;
import com.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookApiController {

    private final BookService bookService;
    private final BookSearchClient bookSearchClient;

    // 전체 조회 (페이징)
    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookKey,desc") String sort) {

        String[] sortParams = sort.split(",");
        String field = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));
        return ResponseEntity.ok(PageResponse.from(bookService.findAll(pageable)));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable String id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    // 등록
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody BookCreateRequest request) {
        return ResponseEntity.ok(bookService.saveBook(request));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable String id, @RequestBody BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    // 전체 조회 (test-bookes 동기화용, 페이징 없음)
    @GetMapping("/list/all")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // ES 검색 + 로그 저장
    @GetMapping("/search/filter")
    public ResponseEntity<PageResponse<BookSearchResponse>> searchWithFilter(
            @ModelAttribute BookSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<BookSearchResponse> result = bookSearchClient.searchWithFilter(request, page, size);

        // 검색어 있을 때만 ES에 로그 저장
        if (request.bookName() != null && !request.bookName().isBlank()) {
            bookSearchClient.saveSearchLog(request.bookName(), (int) result.totalElements());
        }

        return ResponseEntity.ok(result);
    }

    // 검색어 자동완성
    @GetMapping("/search/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "bookName") String field) {
        return ResponseEntity.ok(bookSearchClient.autocomplete(keyword, field));
    }

    // 최근 검색어 (ES Aggregation)
    @GetMapping("/search/recent")
    public ResponseEntity<List<String>> getRecentKeywords() {
        return ResponseEntity.ok(bookSearchClient.getRecentKeywords());
    }

    // 인기 검색어 (ES Aggregation)
    @GetMapping("/search/popular")
    public ResponseEntity<List<String>> getPopularKeywords() {
        return ResponseEntity.ok(bookSearchClient.getPopularKeywords());
    }

    // 최근 검색어 전체 삭제
    @DeleteMapping("/search/recent/all")
    public ResponseEntity<Void> deleteAllRecentKeywords() {
        bookSearchClient.deleteAllRecentKeywords();
        return ResponseEntity.ok().build();
    }

}