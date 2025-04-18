package com.zeewain.socket.protocol;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * 定义通信协议的，这层主要放的是头部信息， body 里边放业务信息
 * <p>
 */
@Data
public class Message implements Serializable {

    /**
     * 消息序号
     */
    // @NotNull
    private Integer id;

    /**
     * 消息的编解码， 0=json,1=protobuf, 解析之后肯定有值，
     * 异常响应的时候会根据请求的 codec 来
     */
    private Byte codec/* = ProtocolConstants.CODEC*/;

    /**
     * 消息的压缩算法， 0 表示不压缩
     */
    private Byte compressor = ProtocolConstants.COMPRESSOR;

    /**
     * 区分当前 message 是请求还是响应， 主要用于异步发起请求在等待响应的时候，
     * 得对应下次过来的数据是请求还是响应类型，不然可能在前后端id生成一致的情况下，误把请求当作响应的方式来做处理
     */
    private boolean request = true;

    /**
     * 消息类型，先使用定长的 String 类型， 兼容之前的接口
     */
    // @NotNull
    private String msgCode;

    /**
     * 消息头，可放用户等信息, 后续支持拦截器，或者负载均衡
     */
    // @Nullable
    private Map<String, String> headMap = new HashMap<>();

    /**
     * 消息体, 可空，没有入参的请求
     */
    // @Nullable
    private Object body;


    /**
     * 这个接口接受的入参类型，（暂未支持）
     */
    byte[] consumes;

    /**
     * 这个接口支持的响应类型，（暂未支持）
     */
    byte[] produces;


}
