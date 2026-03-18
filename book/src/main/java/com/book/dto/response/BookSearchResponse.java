package com.book.dto.response;

import java.time.LocalDate;

// 도서 1건의 데이터
public record BookSearchResponse(
        String bookKey,
        String isbn,
        String bookName,
        String authName,
        LocalDate pubDate,
        String pubName,
        Integer bookPrice,
        String category,
        String categoryName
) {}