package com.a.user.exception;

import com.a.user.exception.errorCode.ErrorCode;
import lombok.Getter;

/**
 * 비즈니스 로직 예외
 * ErrorCode 를 담아서 던지면 GlobalExceptionHandler 에서 처리
 *
 * 사용 예:
 *   throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
