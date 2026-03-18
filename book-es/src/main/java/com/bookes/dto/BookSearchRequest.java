package com.bookes.dto;

public record BookSearchRequest(
        String bookName,
        String authName,
        String pubName,
        Integer minPrice,
        Integer maxPrice,
        String category
) {}
