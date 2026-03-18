package com.bookes.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookCreatedTopic() {
        return TopicBuilder.name("book-created")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookUpdatedTopic() {
        return TopicBuilder.name("book-updated")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bookDeletedTopic() {
        return TopicBuilder.name("book-deleted")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
