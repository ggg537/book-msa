package com.book.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${book-es.url}")
    private String bookEsUrl;

    @Bean
    public RestClient bookEsRestClient(){
        return RestClient.builder()
                .baseUrl(bookEsUrl)     //  http://localhost:8081
                .build();
    }
}
