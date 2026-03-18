package com.book.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),

    // 도서
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "도서를 찾을 수 없습니다."),
    BOOK_DUPLICATE_KEY(HttpStatus.CONFLICT, "이미 존재하는 도서 키입니다."),
    BOOK_DUPLICATE_ISBN(HttpStatus.CONFLICT, "이미 존재하는 ISBN입니다."),

    // 검색
    SEARCH_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "검색 서버와 통신 중 오류가 발생했습니다."),

    // 검색 로그
    SEARCH_LOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "검색어 로그 저장에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}