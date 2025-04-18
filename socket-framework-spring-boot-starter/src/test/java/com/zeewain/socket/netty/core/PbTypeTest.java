package com.zeewain.socket.netty.core;

import com.alibaba.fastjson.JSON;
import com.zeewain.socket.netty.core.dto.PersonProto;
import com.zeewain.socket.netty.core.dto.TtsVo;
import com.zeewain.socket.netty.NettyProto;
import com.zeewain.socket.netty.NettyResponse;
import com.zeewain.socket.netty.codec.protobuf.ProtobufConvertUtil;
import org.junit.jupiter.api.Test;

/**
 * @author stan
 * @date 2024/9/12
 */
public class PbTypeTest {


    /**
     * 响应值的大小，可以直接缩小 10 倍
     */
    @Test
    public void testNettyResponseSize() {
        NettyResponse<Void> resp = NettyResponse.success();
        resp.clearUnused();
        byte[] jsonBytes = JSON.toJSONBytes(resp);

        byte[] protobufBytes = NettyProto.NettyResponse.newBuilder()
                .setCode(resp.getCode())
                .setSuccess(resp.isSuccess())
                .build().toByteArray();

        System.out.println("jsonBytes:" + jsonBytes.length);
        System.out.println("protobufBytes:" + protobufBytes.length);
    }/*
    jsonBytes:25
    protobufBytes:2
    */


    @Test
    void testConvertToTtsParam() {
        TtsVo ttsVo = new TtsVo();
        ttsVo.setVolume(0);
        // ttsVo.setFormat("");
        System.out.println("v1:" + ttsVo);

        // 手动转换的方式，
        // 因为默认值字符串强制为 "" 或者为 null,
        // 数字 的时候为0
        TtsVo ttsVo2 = convert(ttsVo);
        System.out.println("v2:" + ttsVo2);

        // json的方式，0没能传递过来
        TtsVo ttsVo3 = convert2(ttsVo);
        System.out.println("v3:" + ttsVo3);
    }

    private static TtsVo convert(TtsVo ttsVo) {
        PersonProto.TtsVo ttsVoPb = ttsVo.convertToProtobuf(PersonProto.TtsVo.class);
        TtsVo ttsVo2 = new TtsVo();
        ttsVo2.convertFromProtobuf(ttsVoPb);
        return ttsVo2;
    }

    private static TtsVo convert2(TtsVo ttsVo) {
        PersonProto.TtsVo ttsVoPb = ProtobufConvertUtil.convertToProtobufByJson(ttsVo, PersonProto.TtsVo.class);
        return ProtobufConvertUtil.convertFromProtobufByJson(ttsVoPb, TtsVo.class);
    }


}
