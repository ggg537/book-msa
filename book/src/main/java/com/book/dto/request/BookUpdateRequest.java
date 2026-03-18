package com.book.dto.request;

import java.time.LocalDate;

// 수정
public record BookUpdateRequest(
        String bookName,
        String authName,
        LocalDate pubDate,
        String bookIndex,
        String pubName,
        Integer bookPrice,
        String category
) {}
