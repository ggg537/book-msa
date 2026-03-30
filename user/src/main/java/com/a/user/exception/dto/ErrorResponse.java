package com.a.user.exception.dto;

import com.a.user.exception.errorCode.ErrorCode;
import lombok.Getter;

/**
 * 에러 응답 DTO
 * GlobalExceptionHandler 에서 생성해서 반환
 */
@Getter
public class ErrorResponse {

    private final int    status;
    private final String code;
    private final String message;

    private ErrorResponse(ErrorCode errorCode) {
        this.status  = errorCode.getHttpStatus().value();
        this.code    = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode);
    }
}
