package com.zeewain.cbb.netty.protocol;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 定义协议
 *
 * @author stan
 * @description
 * @date 2022/7/22
 */
public interface ProtocolConstants {


    /**
     * Max frame length
     */
    int MAX_FRAME_LENGTH = 8 * 1024 * 1024;


    /**
     * Magic code, 紫为AI
     */
    byte[] MAGIC_CODE_BYTES = {'Z', 'W', 'A', 'I'};

    /**
     * Protocol version
     */
    byte VERSION = 1;

    /**
     * 消息体的序列化和反序列化算法，0表示json
     */
    byte CODEC = Byte.parseByte(System.getProperty("CONFIG_NETTY_CODEC", "0"));

    /**
     * 压缩算法， 0 表示不压缩
     */
    byte COMPRESSOR = Byte.parseByte(System.getProperty("CONFIG_NETTY_COMPRESSOR", "0"));

    /**
     * 需要进行压缩的临界值
     */
    int COMPRESS_THRESHOLD = Integer.parseInt(System.getProperty("CONFIG_NETTY_COMPRESS_THRESHOLD", "2048"));


    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * HEAD_LENGTH of protocol v1
     * 目前第一版协议第一行的头部信息固定长度为16
     */
    int V1_HEAD_LENGTH = 16;

    /**
     * 判断当前消息是不是响应值
     */
    byte IS_RESPONSE = 0b00000001;


    static boolean matchMagicCode(byte[] magicCodes) {
        return Arrays.equals(MAGIC_CODE_BYTES, magicCodes);
    }


}
