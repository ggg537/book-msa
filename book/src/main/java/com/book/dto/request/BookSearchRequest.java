package com.book.dto.request;

// 검색 조건
public record BookSearchRequest(
        String bookName,
        String authName,
        String pubName,
        String category,
        Integer minPrice,
        Integer maxPrice
) {}