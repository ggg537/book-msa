package com.a.bookai.dto;

public record ChatRequest(
        String sessionId,   // 대화 세션 구분
        String message      // 사용자 메시지
) {}