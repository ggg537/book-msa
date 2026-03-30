package com.a.bookai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    당신은 도서 추천 전문가입니다.
                    사용자가 원하는 책을 찾을 수 있도록 친절하게 안내해주세요.

                    반드시 아래 규칙을 따르세요:
                    1. 도서 목록이 제공되면 반드시 그 목록 안에 있는 책만 추천하세요.
                    2. 제공된 도서 목록에 없는 책은 절대 추천하지 마세요.
                    3. 관련 도서가 여러 권이면 최대 5권까지 추천하세요.
                    4. 도서 목록이 비어있을 때만 "현재 등록된 관련 도서가 없습니다"라고 안내하세요.
                    5. 도서 추천 시 제목, 저자, 가격 정보를 함께 제공하세요.
                    6. 가격이 null 이면 "가격 정보 없음" 으로 표시하세요.
                    7. 도서와 관련 없는 질문에는 도서 관련 주제로 안내해주세요.
                    """)
                .build();
    }
}