package com.zeewain.cbb.netty.protocol.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zeewain.socket.netty.NettyResponse;
import com.zeewain.socket.netty.codec.json.JsonCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author stan
 * @date 2022/9/13
 */
class JsonCodecTest {

    @Test
    void encode() {
    }

    @Test
    void decode() {
        JsonCodec codec = new JsonCodec();
        NettyResponse<Boolean> req = NettyResponse.success(true);
        // System.out.println(req);
        byte[] json = codec.encode(req);
        NettyResponse<Boolean> resp = codec.decode(json, new TypeReference<NettyResponse<Boolean>>() {
        }.getType());
        // System.out.println(resp);
        Assertions.assertEquals(req.getData(), resp.getData());
    }

    @Test
    void testDecode() {
    }

    @Test
    public void testError() {
        String payload = "{\"aa\":{\"@type\":\"java.lang.Class\",\"val\":\"com.sun.rowset.JdbcRowSetImpl\"}}";
        JSONObject jsonObject = JSON.parseObject(payload);
        System.out.println(jsonObject);

        // class com.sun.rowset.JdbcRowSetImpl
        // 能通过反射直接拿到指定类
        System.out.println(jsonObject.get("aa"));
    }



}