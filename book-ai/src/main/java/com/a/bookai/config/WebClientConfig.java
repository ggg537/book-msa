package com.a.bookai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${book-search.url}")
    private String bookSearchUrl;

    @Bean
    public WebClient bookSearchWebClient() {
        return WebClient.builder()
                .baseUrl(bookSearchUrl)
                .build();
    }
}