package com.zeewain.cbb.common.core.exception;

import com.zeewain.cbb.common.core.constant.StatusCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 * 基础异常类
 * @author zwl
 * @version 2021年12月13日
 * @since
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    protected int statusCode;

    public BaseException(StatusCodeEnum statusCodeEnum) {
        super(statusCodeEnum.getMessage());
        this.statusCode = statusCodeEnum.code;
    }

    public BaseException(int statusCode) {
        super(Objects.requireNonNull(StatusCodeEnum.codeOf(statusCode)).getMessage());
        this.statusCode = statusCode;
    }

    public BaseException(String message) {
        super(message);
        this.statusCode = StatusCodeEnum.BASE_ERROR.getCode();
    }

    public BaseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public BaseException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
