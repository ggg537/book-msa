package com.bookes.dto;

public record BookSearchResponse(
        String bookKey,
        String isbn,
        String bookName,
        String authName,
        String pubDate,
        String bookIndex,
        String pubName,
        Integer bookPrice,
        String category,
        String categoryName
) {}

