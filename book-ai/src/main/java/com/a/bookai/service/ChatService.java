package com.a.bookai.service;

import com.a.bookai.client.BookSearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final BookSearchClient bookSearchClient;

    private final InMemoryChatMemoryRepository memoryRepository = new InMemoryChatMemoryRepository();

    public String chat(String sessionId, String userMessage) {

        ChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(memoryRepository)
                .build();

        String keyword = extractKeyword(userMessage);
        log.info("추출된 키워드: {}", keyword);

        String searchKeyword = (keyword == null || keyword.isBlank() || "없음".equals(keyword))
                ? userMessage
                : keyword;

        List<Map<String, Object>> books = bookSearchClient.searchBooks(searchKeyword);
        String bookContext = buildBookContext(books);
        log.info("검색 키워드: {}, 검색된 도서 수: {}", searchKeyword, books.size());

        String prompt = bookContext.isBlank()
                ? userMessage
                : bookContext + "\n\n사용자 질문: " + userMessage;

        return chatClient.prompt()
                .user(prompt)
                .advisors(MessageChatMemoryAdvisor.builder(memory)
                        .conversationId(sessionId)
                        .build())
                .call()
                .content();
    }

    // 사용자 메시지에서 도서 검색 키워드 추출
    private String extractKeyword(String userMessage) {
        try {
            return chatClient.prompt()
                    .user("""
                    아래 문장에서 도서 검색에 사용할 핵심 키워드 1개만 추출해주세요.
                    도서와 관련 없는 문장이면 "없음" 이라고만 답하세요.
                    키워드만 단답으로 답하세요. 설명하지 마세요.
                    
                    문장: """ + userMessage)
                    .call()
                    .content()
                    .trim();
        } catch (Exception e) {
            log.error("키워드 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    // 도서 목록을 Gemini에 전달할 텍스트로 변환
    private String buildBookContext(List<Map<String, Object>> books) {
        if (books.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("아래는 검색된 도서 목록입니다. 반드시 이 목록에 있는 책만 추천하세요:\n\n");
        books.forEach(book -> sb.append(String.format(
                "- 제목: %s | 저자: %s | 출판사: %s | 가격: %s원\n",
                book.get("bookName"),
                book.get("authName"),
                book.get("pubName"),
                book.get("bookPrice") != null ? book.get("bookPrice") : "정보 없음"
        )));
        return sb.toString();
    }

    // 세션 대화 기록 삭제
    public void clearSession(String sessionId) {
        memoryRepository.deleteByConversationId(sessionId);
        log.info("세션 삭제 - sessionId: {}", sessionId);
    }
}