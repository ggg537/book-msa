package com.bookes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "books";

    /**
     * 인덱스 생성 + 매핑
     */
    public String createIndex() throws Exception {
        if (existsIndex()) {
            deleteIndex();
        }

        String settings = """
                {
                    "number_of_shards": 1,
                    "number_of_replicas": 0,
                    "analysis": {
                        "filter": {
                            "nori_posfilter": {
                                "type": "nori_part_of_speech",
                                "stoptags": [
                                    "E",
                                    "IC",
                                    "J",
                                    "MAG",
                                    "MM",
                                    "SP",
                                    "SSC",
                                    "SSO",
                                    "SC",
                                    "SE",
                                    "XPN",
                                    "XSA",
                                    "XSN",
                                    "XSV",
                                    "UNA",
                                    "VSV"
                                ]
                            }
                        },
                        "tokenizer": {
                            "nori_tok": {
                                "type": "nori_tokenizer",
                                "decompound_mode": "mixed"
                            },
                            "edge_ngram_tok": {
                                "type": "edge_ngram",
                                "min_gram": 1,
                                "max_gram": 20
                            }
                        },
                        "analyzer": {
                            "nori_analyzer": {
                                "type": "custom",
                                "tokenizer": "nori_tok",
                                "filter": ["nori_posfilter", "lowercase"]
                            },
                            "autocomplete_analyzer": {
                                "type": "custom",
                                "tokenizer": "edge_ngram_tok",
                                "filter": ["lowercase"]
                            },
                            "autocomplete_search_analyzer": {
                                "type": "custom",
                                "tokenizer": "standard",
                                "filter": ["lowercase"]
                            }
                        }
                    }
                }
                """;

        String mappings = """
                {
                    "properties": {
                        "bookKey": { "type": "keyword" },
                        "isbn":    { "type": "keyword" },
                        "bookName": {
                            "type": "text",
                            "analyzer": "nori_analyzer",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                },
                                "autocomplete": {
                                    "type": "text",
                                    "analyzer": "autocomplete_analyzer",
                                    "search_analyzer": "autocomplete_search_analyzer"
                                },
                                "with_josa": {
                                    "type": "text",
                                    "analyzer": "standard"
                                }
                            }
                        },
                        "authName": {
                            "type": "text",
                            "analyzer": "nori_analyzer",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                },
                                "autocomplete": {
                                    "type": "text",
                                    "analyzer": "autocomplete_analyzer",
                                    "search_analyzer": "autocomplete_search_analyzer"
                                }
                            }
                        },
                        "pubName": {
                            "type": "text",
                            "analyzer": "nori_analyzer",
                            "fields": {
                                "keyword": {
                                    "type": "keyword"
                                },
                                "autocomplete": {
                                    "type": "text",
                                    "analyzer": "autocomplete_analyzer",
                                    "search_analyzer": "autocomplete_search_analyzer"
                                }
                            }
                        },
                        "pubDate":   { "type": "date", "format": "yyyy-MM-dd" },
                        "bookIndex": { "type": "text", "analyzer": "nori_analyzer" },
                        "bookPrice": { "type": "integer" },
                        "category":  { "type": "keyword" }
                    }
                }
                """;

        CreateIndexResponse response = esClient.indices().create(c -> c
                .index(INDEX_NAME)
                .settings(s -> s.withJson(new StringReader(settings)))
                .mappings(m -> m.withJson(new StringReader(mappings)))
        );

        log.info("인덱스 생성 완료: {}", response.acknowledged());
        return "인덱스 생성 완료: " + INDEX_NAME;
    }

    /**
     * search_log 인덱스 생성
     * keyword: 집계용 keyword 타입
     * searchDate: 기간 필터용 date 타입
     * resultCount: 검색 결과 수
     */
    public String createSearchLogIndex() throws Exception {
        String LOG_INDEX = "search_log";

        if (esClient.indices().exists(e -> e.index(LOG_INDEX)).value()) {
            esClient.indices().delete(d -> d.index(LOG_INDEX));
            log.info("search_log 인덱스 삭제 완료");
        }

        String mappings = """
                {
                    "properties": {
                        "keyword":     { "type": "keyword" },
                        "searchDate":  { "type": "date",    "format": "yyyy-MM-dd'T'HH:mm:ss" },
                        "resultCount": { "type": "integer" }
                    }
                }
                """;

        esClient.indices().create(c -> c
                .index(LOG_INDEX)
                .mappings(m -> m.withJson(new StringReader(mappings)))
        );

        log.info("search_log 인덱스 생성 완료");
        return "search_log 인덱스 생성 완료";
    }

    /**
     * 인덱스 삭제
     */
    public String deleteIndex() throws Exception {
        esClient.indices().delete(d -> d.index(INDEX_NAME));
        log.info("인덱스 삭제 완료: {}", INDEX_NAME);
        return "인덱스 삭제 완료: " + INDEX_NAME;
    }

    /**
     * 인덱스 존재 여부 확인
     */
    public boolean existsIndex() throws Exception {
        return esClient.indices().exists(e -> e.index(INDEX_NAME)).value();
    }

    /**
     * 인덱스 정보 조회
     */
    public String getIndexInfo() throws Exception {
        if (!existsIndex()) {
            return "인덱스가 존재하지 않습니다: " + INDEX_NAME;
        }
        var info = esClient.indices().get(g -> g.index(INDEX_NAME));
        return info.get(INDEX_NAME).toString();
    }
}