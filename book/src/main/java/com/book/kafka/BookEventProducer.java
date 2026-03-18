package com.book.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // 토픽 상수
    private static final String TOPIC_BOOK_CREATED = "book-created";
    private static final String TOPIC_BOOK_UPDATED = "book-updated";
    private static final String TOPIC_BOOK_DELETED = "book-deleted";

    // 도서 등록 이벤트
    public void sendCreated(String bookKey, Object bookData) {
        send(TOPIC_BOOK_CREATED, bookKey, bookData);
    }

    // 도서 수정 이벤트
    public void sendUpdated(String bookKey, Object bookData) {
        send(TOPIC_BOOK_UPDATED, bookKey, bookData);
    }

    // 도서 삭제 이벤트
    public void sendDeleted(String bookKey) {
        send(TOPIC_BOOK_DELETED, bookKey, Map.of("bookKey", bookKey));
    }

    private void send(String topic, String key, Object data) {
        try {
            String message = objectMapper.writeValueAsString(data);
            kafkaTemplate.send(topic, key, message);
            log.info("Kafka 이벤트 발행 - topic: {}, key: {}", topic, key);
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패 - topic: {}, key: {}, error: {}", topic, key, e.getMessage());
        }
    }
}
