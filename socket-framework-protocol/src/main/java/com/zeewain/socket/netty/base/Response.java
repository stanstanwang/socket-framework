package com.zeewain.socket.netty.base;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Response<T> {
    private T data;

    /**
     * 响应结果， 非0都表示异常
     */
    private int code;

    /**
     * 成功或错误信息
     */
    private String message;

    public Response() {

    }
}
