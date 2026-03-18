package com.book.domain;

import com.book.dto.request.BookUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "test_book_tbl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Book {

    @Id
    @Column(name = "LBP_BOOK_KEY", length = 15)
    private String bookKey;

    @Column(name = "LBP_ISBN", nullable = false, length = 32, unique = true)
    private String isbn;

    @Column(name = "LBP_BOOK_NAME", nullable = false, length = 256)
    private String bookName;

    @Column(name = "LBP_AUTH_NAME", length = 128)
    private String authName;

    @Column(name = "LBP_PUB_DATE")
    private LocalDate pubDate;

    @Column(name = "LBP_BOOK_INDEX", columnDefinition = "TEXT")
    private String bookIndex;

    @Column(name = "LBP_PUB_NAME", length = 50)
    private String pubName;

    @Column(name = "LBP_BOOK_PRICE")
    private Integer bookPrice;

    @Column(name = "LBP_CATEGORY", length = 10)
    private String category;


    public static Book of(String bookKey, String isbn, String bookName, String authName,
            LocalDate pubDate, String bookIndex, String pubName, Integer bookPrice, String category) {
        return new Book(bookKey, isbn, bookName, authName, pubDate, bookIndex, pubName, bookPrice, category);
    }

    public void update(BookUpdateRequest request) {
        this.bookName = request.bookName();
        this.authName = request.authName();
        this.pubDate = request.pubDate();
        this.bookIndex = request.bookIndex();
        this.pubName = request.pubName();
        this.bookPrice = request.bookPrice();
        this.category = request.category();
    }
}
