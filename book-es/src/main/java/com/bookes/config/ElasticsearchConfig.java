package com.bookes.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.url}")
    private String url;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 1. HTTP 통신
        RestClient restClient = RestClient.builder(HttpHost.create(url)).build();

        // 2. JSON 변환
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // 3. 우리가 사용할 클라이언트
        return new ElasticsearchClient(transport);
    }
}