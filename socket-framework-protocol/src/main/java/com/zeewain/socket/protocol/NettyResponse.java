package com.zeewain.socket.protocol;

import com.zeewain.socket.protocol.base.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * netty 专用的响应类型, 新增了 msgCode 字段, 用于告知客户端具体的消息类型
 * 20220801 目前这个类有些尴尬， 响应按理来说客户端应该是根据id来对应上的，而不是使用 msgCode,
 * 所以直接用公共的 Response 便可, 另外就算有 msgCode 也不是放在这里的， 而是放在协议上的
 * <p>
 * // TODO delay stan 2022/8/22 给响应数据的话， msgCode 不是必须的， 所以沿用 Reponse 便可
 *
 * @author stan
 * @date 2022/7/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NettyResponse<T> extends Response<T> {


    /**
     * 消息类型， socket 通信必须使用
     */
    // TODO stan 2025/4/18 删掉 msgCode
    private String msgCode;


    public NettyResponse() {
    }

    private NettyResponse(String msgCode) {
        this.msgCode = msgCode;
    }

    private NettyResponse(String msgCode, T data) {
        this.msgCode = msgCode;
        this.setData(data);
    }

    public static NettyResponse<Void> success() {
        return new NettyResponse<Void>().clearUnused();
    }

    /**
     * 响应成功信息
     */
    public static <T> NettyResponse<T> success(T data) {
        return success(null, data);
    }

    // msgType 已经不用了， 慢慢忽略
    @Deprecated
    public static <T> NettyResponse<T> success(String msgType) {
        return success(msgType, null);
    }

    @Deprecated
    public static <T> NettyResponse<T> success(String msgType, T data) {
        return new NettyResponse<>(msgType, data).clearUnused();
    }


    /**
     * 响应失败信息
     */
    public static <T> NettyResponse<T> error(int code) {
        return error(code, null);
    }

    public static <T> NettyResponse<T> error(int code, String message) {
        NettyResponse<T> resp = new NettyResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }

    public boolean isSuccess() {
        return getCode() == 0;
    }


    public static NettyResponse<Object> fromProtobuf(NettyProto.NettyResponse resp) {
        NettyResponse<Object> r = new NettyResponse<>();
        r.setCode(resp.getCode());
        r.setMessage(resp.getMessage());
        return r;
    }

    public NettyProto.NettyResponse toProtobuf() {
        NettyProto.NettyResponse.Builder builder = NettyProto.NettyResponse.newBuilder();
        builder.setCode(this.getCode());
        if (this.getMessage() != null) {
            builder.setMessage(this.getMessage());
        }
        // 后边看怎么转换
        // builder.setData(this.getData());
        return builder.build();
    }


    /**
     * 清理没使用到的字段，简化协议
     */
    public NettyResponse<T> clearUnused() {
        // 去掉 response 的 msgCode， 这样好处很多，写接口文档可以更清晰些，而不是成倍增长
        // 响应加 msgCode 这种有些恶心， 应该只需要一个 resp 的标识便可
        this.setMsgCode(null);
        // 成功的话 message 没意义，占用空间
        if (this.isSuccess()) {
            this.setMessage(null);
        }
        return this;
    }


}
