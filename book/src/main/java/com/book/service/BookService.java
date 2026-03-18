package com.book.service;

import com.book.domain.Book;
import com.book.dto.request.BookCreateRequest;
import com.book.dto.request.BookUpdateRequest;
import com.book.dto.response.BookResponse;
import com.book.exception.BusinessException;
import com.book.exception.errorCode.ErrorCode;
import com.book.kafka.BookEventProducer;
import com.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookEventProducer bookEventProducer;  // ✅ 추가

    // 등록
    @Transactional
    public BookResponse saveBook(BookCreateRequest request) {
        Book book = bookRepository.save(request.toEntity());
        BookResponse response = BookResponse.from(book);
        bookEventProducer.sendCreated(book.getBookKey(), response);  // ✅ 추가
        return response;
    }

    // 단건 조회
    public BookResponse findById(String id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        return BookResponse.from(book);
    }

    // 전체 조회
    public Page<BookResponse> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(BookResponse::from);
    }

    // 수정
    @Transactional
    public BookResponse updateBook(String id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        book.update(request);
        BookResponse response = BookResponse.from(book);
        bookEventProducer.sendUpdated(book.getBookKey(), response);  // ✅ 추가
        return response;
    }

    // 삭제
    @Transactional
    public void deleteBook(String id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));
        bookRepository.delete(book);
        bookEventProducer.sendDeleted(book.getBookKey());  // ✅ 추가
    }

    // test-book-es 동기화(Sync)용 API
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }
}
