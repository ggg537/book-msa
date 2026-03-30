package com.a.user.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    MEMBER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    MEMBER_DEACTIVATED(HttpStatus.FORBIDDEN, "탈퇴한 회원입니다."),
    MEMBER_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),

    POINT_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "포인트가 부족합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}