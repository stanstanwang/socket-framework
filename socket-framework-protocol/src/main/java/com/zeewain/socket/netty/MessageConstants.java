package com.zeewain.socket.netty;

/**
 * 定义了消息的通用常量
 *
 * @author stan
 * @date 2022/7/29
 */
public interface MessageConstants {

    // 无应答的心跳消息， 服务端和客户端都可以发送
    // 目前采用的心跳方式是 服务端和客户端双向发送， 这样在无应答请求的时候可以少发送一些消息
    String HEARTBEAT = "HEARTBEAT";

    // 心跳消息
    String HEARTBEAT_REQ = "HEARTBEAT_REQ";

    // 心跳应答
    String HEARTBEAT_RESP = "HEARTBEAT_RESP";

    // 服务端的错误信息
    String ERROR = "ERROR";


    String CLIENT_HELLO = "CLIENT_HELLO";
    String SERVER_HELLO = "SERVER_HELLO";




}
