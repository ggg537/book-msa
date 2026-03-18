package com.book.dto.request;

import com.book.domain.Book;
import java.time.LocalDate;

public record BookCreateRequest(
        String bookKey,
        String isbn,
        String bookName,
        String authName,
        LocalDate pubDate,
        String bookIndex,
        String pubName,
        Integer bookPrice,
        String category
) {
    public Book toEntity() {
        return Book.of(bookKey, isbn, bookName, authName, pubDate, bookIndex, pubName, bookPrice, category);  // ✅ category 추가
    }
}