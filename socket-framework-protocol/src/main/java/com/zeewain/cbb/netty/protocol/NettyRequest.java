package com.zeewain.cbb.netty.protocol;

import lombok.Data;

/**
 * @author stan
 * @description
 * @date 2022/7/26
 */
@Data
public class NettyRequest<T> {


    /**
     * 消息类型， socket 通信必须使用
     */
    private String msgCode;

    private T data;


    public NettyRequest() {
    }

    public NettyRequest(String msgCode) {
        this.msgCode = msgCode;
    }

    public NettyRequest(String msgCode, T data) {
        this.msgCode = msgCode;
        this.data = data;
    }


    /**
     * 响应成功信息
     */
    public static <T> NettyRequest<T> of(String msgType) {
        return of(msgType, null);
    }

    public static <T> NettyRequest<T> of(String msgType, T data) {
        return new NettyRequest<>(msgType, data);
    }

}
