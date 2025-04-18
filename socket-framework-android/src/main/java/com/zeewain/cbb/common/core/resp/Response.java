package com.zeewain.cbb.common.core.resp;

import com.zeewain.cbb.common.core.constant.StatusCodeEnum;

import java.time.LocalDateTime;

public class Response<T> {
    private T data;
    private int code;
    private String message;
    private LocalDateTime localTime;

    public Response() {
        this.code = StatusCodeEnum.SUCCESS.code;
        this.message = StatusCodeEnum.SUCCESS.message;
        this.localTime = LocalDateTime.now();
    }

    /** @deprecated */
    @Deprecated
    public Response<T> setCode(StatusCodeEnum code) {
        this.code = code.code;
        return this;
    }

    public Response<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public Response<T> setErrorMessage(String message) {
        if (this.code != StatusCodeEnum.SUCCESS.code) {
            message = StatusCodeEnum.codeOf(this.code).message + ", " + message;
        }

        this.message = message;
        return this;
    }

    /** @deprecated */
    @Deprecated
    public Response(StatusCodeEnum code, String message) {
        this.code = StatusCodeEnum.SUCCESS.code;
        this.message = StatusCodeEnum.SUCCESS.message;
        this.localTime = LocalDateTime.now();
        this.code = code.code;
        this.message = message;
    }

    /** @deprecated */
    @Deprecated
    public Response(StatusCodeEnum code, String message, T data) {
        this.code = StatusCodeEnum.SUCCESS.code;
        this.message = StatusCodeEnum.SUCCESS.message;
        this.localTime = LocalDateTime.now();
        this.code = code.code;
        this.message = message;
        this.data = data;
    }

    public Response(int code, String message) {
        this.code = StatusCodeEnum.SUCCESS.code;
        this.message = StatusCodeEnum.SUCCESS.message;
        this.localTime = LocalDateTime.now();
        this.code = code;
        this.message = message;
    }

    public Response(int code, String message, T data) {
        this.code = StatusCodeEnum.SUCCESS.code;
        this.message = StatusCodeEnum.SUCCESS.message;
        this.localTime = LocalDateTime.now();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String toString() {
        return "Response{Data=" + this.data + ", code=" + this.code + ", desc='" + this.message + '\'' + ", LocalTime='" + this.localTime + '\'' + '}';
    }

    public T getData() {
        return this.data;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getLocalTime() {
        return this.localTime;
    }

    public Response<T> setData(final T data) {
        this.data = data;
        return this;
    }

    public Response<T> setMessage(final String message) {
        this.message = message;
        return this;
    }

    public Response<T> setLocalTime(final LocalDateTime localTime) {
        this.localTime = localTime;
        return this;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Response)) {
            return false;
        } else {
            Response<?> other = (Response)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getCode() != other.getCode()) {
                return false;
            } else {
                label49: {
                    Object this$data = this.getData();
                    Object other$data = other.getData();
                    if (this$data == null) {
                        if (other$data == null) {
                            break label49;
                        }
                    } else if (this$data.equals(other$data)) {
                        break label49;
                    }

                    return false;
                }

                Object this$message = this.getMessage();
                Object other$message = other.getMessage();
                if (this$message == null) {
                    if (other$message != null) {
                        return false;
                    }
                } else if (!this$message.equals(other$message)) {
                    return false;
                }

                Object this$localTime = this.getLocalTime();
                Object other$localTime = other.getLocalTime();
                if (this$localTime == null) {
                    if (other$localTime != null) {
                        return false;
                    }
                } else if (!this$localTime.equals(other$localTime)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Response;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + this.getCode();
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        Object $message = this.getMessage();
        result = result * 59 + ($message == null ? 43 : $message.hashCode());
        Object $localTime = this.getLocalTime();
        result = result * 59 + ($localTime == null ? 43 : $localTime.hashCode());
        return result;
    }
}