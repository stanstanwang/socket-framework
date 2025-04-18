package com.zeewain.socket.netty.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class BaseException extends RuntimeException {

    @ToString.Include(rank = 90)
    protected int statusCode;

    @ToString.Include(rank = 60)
    public String getMessage() {
        return super.getMessage();
    }

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
        this.statusCode = StatusCodeEnum.ERROR.getCode();
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
