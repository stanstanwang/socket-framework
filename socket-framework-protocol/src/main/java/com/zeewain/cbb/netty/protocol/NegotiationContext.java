package com.zeewain.cbb.netty.protocol;

import com.zeewain.cbb.netty.protocol.codec.BodyCodec;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 协商的上下文
 * @author stan
 * @date 2023/4/19
 */
public class NegotiationContext {


    public static final AttributeKey<NegotiationInfo> NEGOTIATION_KEY = AttributeKey.valueOf("_negotiation_info");

    public static NegotiationInfo getNegotiationInfo(Channel channel) {
        if (channel.hasAttr(NEGOTIATION_KEY)) {
            return channel.attr(NEGOTIATION_KEY).get();
        } else {
            return NegotiationInfo.V1_DEFAULT;
        }
    }


    /**
     * 当前是否支持 protobuf 的序列化协议
     */
    public static boolean supportProtobuf(Channel channel) {
        return getNegotiationInfo(channel)
                .getSerializationAlgorithms()
                .contains(BodyCodec.CODEC_TYPE_PROTOBUF);
    }
}
