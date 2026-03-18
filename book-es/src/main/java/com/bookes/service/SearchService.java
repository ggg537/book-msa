package com.bookes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.bookes.dto.BookSearchRequest;
import com.bookes.dto.BookSearchResponse;
import com.bookes.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.elastic.clients.elasticsearch._types.query_dsl.Operator.And;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;
    private static final String INDEX_NAME = "books";

    public PageResponse<BookSearchResponse> searchBooks(BookSearchRequest request, int page, int size) throws Exception {

        SearchResponse<BookSearchResponse> response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .bool(b -> buildQuery(b, request))
                        )
                        .from(page * size)
                        .size(size),
                BookSearchResponse.class
        );

        List<BookSearchResponse> content = response.hits().hits().stream()
                .map(Hit::source)
                .toList();

        long totalElements = response.hits().total() != null
                ? response.hits().total().value()
                : 0;

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    public List<String> autocomplete(String keyword, String field) throws Exception {

        String searchField = switch (field) {
            case "authName" -> "authName.autocomplete";
            case "pubName"  -> "pubName.autocomplete";
            default         -> "bookName.autocomplete";
        };

        String sourceField = switch (field) {
            case "authName" -> "authName";
            case "pubName"  -> "pubName";
            default         -> "bookName";
        };

        SearchResponse<BookSearchResponse> response = esClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .match(m -> m
                                        .field(searchField)
                                        .query(keyword)
                                )
                        )
                        .size(20)
                        .source(src -> src
                                .filter(f -> f.includes(sourceField))
                        ),
                BookSearchResponse.class
        );

        return response.hits().hits().stream()
                .map(hit -> {
                    if (hit.source() == null) return "";
                    return switch (field) {
                        case "authName" -> hit.source().authName();
                        case "pubName"  -> hit.source().pubName();
                        default         -> hit.source().bookName();
                    };
                })
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .limit(10)
                .toList();
    }

    private BoolQuery.Builder buildQuery(BoolQuery.Builder b, BookSearchRequest request) {

        if (request.bookName() != null && !request.bookName().isBlank()) {
            String keyword = escapeSpecialChars(request.bookName());  // ✅ 특수문자 처리

            if (!keyword.isBlank()) {
                b.must(m -> m.bool(bool -> bool
                        .must(must -> must.multiMatch(mm -> mm
                                .fields("bookName^2", "bookName.with_josa", "authName", "pubName")
                                .query(keyword)
                                .fuzziness("1")
                                .operator(And)
                                .tieBreaker(0.3)
                        ))
                        .should(should -> should.match(mt -> mt
                                .field("bookName.keyword")
                                .query(keyword)
                                .boost(100.0f)
                        ))
                        .should(should -> should.matchPhrase(mp -> mp
                                .field("bookName")
                                .query(keyword)
                                .boost(30.0f)
                        ))
                        .should(should -> should.matchPhrase(mp -> mp
                                .field("bookName.with_josa")
                                .query(keyword)
                                .boost(20.0f)
                        ))
                ));
            }
        }

        // 저자 상세 검색
        if (request.authName() != null && !request.authName().isBlank()) {
            b.must(m -> m.match(t -> t.field("authName").query(request.authName()).operator(And)));
        }

        // 출판사 상세 검색
        if (request.pubName() != null && !request.pubName().isBlank()) {
            b.must(m -> m.match(t -> t.field("pubName").query(request.pubName()).operator(And)));
        }

        // 가격 필터
        if (request.minPrice() != null) {
            b.filter(f -> f.range(r -> r.number(n -> n.field("bookPrice").gte(request.minPrice().doubleValue()))));
        }
        if (request.maxPrice() != null) {
            b.filter(f -> f.range(r -> r.number(n -> n.field("bookPrice").lte(request.maxPrice().doubleValue()))));
        }

        // ✅ 카테고리 필터 추가
        if (request.category() != null && !request.category().isBlank()) {
            b.filter(f -> f.term(t -> t.field("category").value(request.category())));
        }

        return b;
    }

    // ✅ 특수문자 처리 메서드 추가
    private String escapeSpecialChars(String keyword) {
        return keyword
                .replaceAll("[+\\-=&|><!(){ }\\[\\]^\"~*?:\\\\/]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
