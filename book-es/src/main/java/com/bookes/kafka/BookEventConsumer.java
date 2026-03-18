package com.bookes.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventConsumer {

    private final ElasticsearchClient esClient;
    private final ObjectMapper objectMapper;

    private static final String INDEX_NAME = "books";

    // 도서 등록 이벤트 수신
    @KafkaListener(topics = "book-created", groupId = "book-es-group")
    public void handleCreated(String message) {
        try {
            Map<String, Object> book = objectMapper.readValue(message, new TypeReference<>() {});
            String bookKey = String.valueOf(book.get("bookKey"));

            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(bookKey)
                    .document(book)
            );
            log.info("ES 도서 등록 완료 - bookKey: {}", bookKey);
        } catch (Exception e) {
            log.error("ES 도서 등록 실패 - error: {}", e.getMessage());
        }
    }

    // 도서 수정 이벤트 수신
    @KafkaListener(topics = "book-updated", groupId = "book-es-group")
    public void handleUpdated(String message) {
        try {
            Map<String, Object> book = objectMapper.readValue(message, new TypeReference<>() {});
            String bookKey = String.valueOf(book.get("bookKey"));

            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(bookKey)
                    .document(book)
            );
            log.info("ES 도서 수정 완료 - bookKey: {}", bookKey);
        } catch (Exception e) {
            log.error("ES 도서 수정 실패 - error: {}", e.getMessage());
        }
    }

    // 도서 삭제 이벤트 수신
    @KafkaListener(topics = "book-deleted", groupId = "book-es-group")
    public void handleDeleted(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, new TypeReference<>() {});
            String bookKey = String.valueOf(data.get("bookKey"));

            esClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(bookKey)
            );
            log.info("ES 도서 삭제 완료 - bookKey: {}", bookKey);
        } catch (Exception e) {
            log.error("ES 도서 삭제 실패 - error: {}", e.getMessage());
        }
    }
}
