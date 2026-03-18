package com.bookes.controller;

import com.bookes.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<String> syncBooks() throws Exception {
        return ResponseEntity.ok(syncService.syncBooks());
    }
}