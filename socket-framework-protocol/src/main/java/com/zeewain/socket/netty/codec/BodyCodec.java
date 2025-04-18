package com.zeewain.socket.netty.codec;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author stan
 * @description
 * @date 2022/7/22
 */
public interface BodyCodec {


    byte CODEC_TYPE_JSON = 0;
    byte CODEC_TYPE_PROTOBUF = 1;

    Map<Byte, BodyCodec> MAP_CACHE = new HashMap<>();


    /**
     * 当前序列化器是否支持某个逻辑
     */
    boolean support(Object obj);

    /**
     * body的序列化算法类型
     */
    byte getType();

    /**
     * 序列化
     */
    byte[] encode(Object obj);

    /**
     * 反序列化，泛型会丢失
     */
    <T> T decode(byte[] data, Class<T> clazz);


    /**
     * 反序列化, 支持泛型
     */
    <T> T decode(byte[] data, Type type);


    /**
     * 根据枚举获取编解码器
     */
    static BodyCodec getByType(byte type) {
        return MAP_CACHE.get(type);
    }


    /**
     * 根据类型获取编解码器
     */
    static BodyCodec getCodec(List<Byte> supportedAlgorithms, Object type) {
        // 看客户端和对应类型是否支持 protobuf 序列化协议
        // 这里如果是 NettyResponse 的话，都很容易误判
        if (supportedAlgorithms.contains(CODEC_TYPE_PROTOBUF)) {
            BodyCodec protobuf = MAP_CACHE.get(CODEC_TYPE_PROTOBUF);
            if (protobuf.support(type)) {
                return protobuf;
            }
        }

        // json 作为兜底方案，应该默认都支持的
        BodyCodec json = MAP_CACHE.get(CODEC_TYPE_JSON);
        if (json.support(type)) {
            return json;
        }

        throw new IllegalStateException(String.format("not found codec for type %s", type));
    }

    static void register(byte type, BodyCodec codec) {
        MAP_CACHE.put(type, codec);
    }

}
