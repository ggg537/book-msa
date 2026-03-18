package com.bookes.controller;

import com.bookes.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexController {

    private final IndexService indexService;

    // 인덱스 생성
    @PostMapping("/create")
    public ResponseEntity<String> createIndex() throws Exception {
        return ResponseEntity.ok(indexService.createIndex());
    }

    // search_log 인덱스 생성
    @PostMapping("/create/search-log")
    public ResponseEntity<String> createSearchLogIndex() throws Exception {
        return ResponseEntity.ok(indexService.createSearchLogIndex());
    }

    // 인덱스 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteIndex() throws Exception {
        return ResponseEntity.ok(indexService.deleteIndex());
    }

    // 인덱스 존재 확인
    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsIndex() throws Exception {
        return ResponseEntity.ok(indexService.existsIndex());
    }

    // 인덱스 정보 조회
    @GetMapping("/info")
    public ResponseEntity<String> getIndexInfo() throws Exception {
        return ResponseEntity.ok(indexService.getIndexInfo());
    }
}
