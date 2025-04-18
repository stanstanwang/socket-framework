package com.zeewain.socket.core.exception;

/**
 * @author stan
 * @date 2022/8/27
 */
public class ConnectException extends RuntimeException {

    public ConnectException() {
    }

    public ConnectException(String message) {
        super(message);
    }

    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
