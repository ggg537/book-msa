package com.book.dto.response;

import com.book.domain.Book;
import com.book.domain.Category;
import java.time.LocalDate;

public record BookResponse(
        String bookKey,
        String isbn,
        String bookName,
        String authName,
        LocalDate pubDate,
        String bookIndex,
        String pubName,
        Integer bookPrice,
        String category,
        String categoryName
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getBookKey(),
                book.getIsbn(),
                book.getBookName(),
                book.getAuthName(),
                book.getPubDate(),
                book.getBookIndex(),
                book.getPubName(),
                book.getBookPrice(),
                book.getCategory(),
                book.getCategory() != null
                        ? Category.fromCode(book.getCategory()).getFullName()
                        : null
        );
    }
}
