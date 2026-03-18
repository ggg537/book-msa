package com.bookes.controller;

import com.bookes.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * 검색 로그 저장
     * test-book-search → BookSearchClient.saveSearchLog() 에서 호출
     */
    @PostMapping("/search")
    public ResponseEntity<Void> saveLog(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int resultCount) throws Exception {
        if (keyword == null || keyword.isBlank()) {  // ✅ 유효성 검증 추가
            return ResponseEntity.badRequest().build();
        }
        logService.saveLog(keyword, resultCount);
        return ResponseEntity.ok().build();
    }

    /**
     * 최근 검색어 TOP 10
     */
    @GetMapping("/recent")
    public ResponseEntity<List<String>> getRecent() throws Exception {
        return ResponseEntity.ok(logService.getRecentKeywords());
    }

    /**
     * 인기 검색어 TOP 10 (Redis ZSet, 전체 기간)  // ✅ 주석 수정
     */
    @GetMapping("/popular")
    public ResponseEntity<List<String>> getPopular() throws Exception {
        return ResponseEntity.ok(logService.getPopularKeywords());
    }

    // 최근 검색어 전체 삭제
    @DeleteMapping("/recent/all")
    public ResponseEntity<Void> deleteAllRecentKeywords() throws Exception {  // ✅ 메서드명 변경
        logService.clearRecentKeywords();  // ✅ 서비스 호출명 변경
        return ResponseEntity.ok().build();
    }
}