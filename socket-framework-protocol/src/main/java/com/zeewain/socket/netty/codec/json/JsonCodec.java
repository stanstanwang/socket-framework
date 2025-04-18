package com.zeewain.socket.netty.codec.json;

import com.alibaba.fastjson.JSON;
import com.zeewain.socket.netty.codec.BodyCodec;

import java.lang.reflect.Type;

/**
 * json 实现的序列化和反序列化, 后续可抽象接口不同实现
 *
 * @author stan
 * @description
 * @date 2022/7/20
 */
public class JsonCodec implements BodyCodec {

    private static final byte[] EMPTY = new byte[0];

    public JsonCodec() {
        BodyCodec.register(getType(), this);
    }

    @Override
    public byte[] encode(Object obj) {
        if (obj == null) {
            return EMPTY;
        }
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T decode(byte[] json, Class<T> clazz) {
        if (json == null || json.length == 0) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    @Override
    public <T> T decode(byte[] json, Type type) {
        if (json == null || json.length == 0) {
            return null;
        }
        return JSON.parseObject(json, type);
    }

    /**
     * json 作为兜底的序列化逻辑
     */
    @Override
    public boolean support(Object obj) {
        return true;
    }

    @Override
    public byte getType() {
        return CODEC_TYPE_JSON;
    }
}
