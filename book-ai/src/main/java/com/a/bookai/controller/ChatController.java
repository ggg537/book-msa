package com.a.bookai.controller;

import com.a.bookai.dto.ChatRequest;
import com.a.bookai.dto.ChatResponse;
import com.a.bookai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        log.info("채팅 요청 - sessionId: {}, message: {}",
                request.sessionId(), request.message());
        String response = chatService.chat(request.sessionId(), request.message());
        return new ChatResponse(request.sessionId(), response);
    }

    @DeleteMapping("/{sessionId}")
    public void clearSession(@PathVariable String sessionId) {
        log.info("세션 종료 - sessionId: {}", sessionId);
        chatService.clearSession(sessionId);
    }
}
