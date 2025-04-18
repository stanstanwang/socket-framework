package com.zeewain.socket.netty;

import com.zeewain.socket.netty.codec.BodyCodec;
import com.zeewain.socket.netty.compress.Compressor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 协商信息
 *
 * @author stan
 * @date 2023/4/19
 */
@Data
public class NegotiationInfo {

    // 第一版协议未支持压缩和其他序列化方式的默认值
    public static final NegotiationInfo V1_DEFAULT;

    // 支持 protobuf 协议后的默认值
    public static final NegotiationInfo V2_DEFAULT;

    static {
        V1_DEFAULT = new NegotiationInfo();
        V1_DEFAULT.serializationAlgorithms = Collections.singletonList(BodyCodec.CODEC_TYPE_JSON);

        V2_DEFAULT = new NegotiationInfo();
        V2_DEFAULT.serializationAlgorithms = Arrays.asList(BodyCodec.CODEC_TYPE_JSON, BodyCodec.CODEC_TYPE_PROTOBUF);
        V2_DEFAULT.compressionAlgorithms = Arrays.asList(Compressor.UN_COMPRESS, Compressor.GZIP_COMPRESS);
    }

    /**
     * 支持的加密协议
     */
    private List<Byte> encryptionAlgorithms = Collections.emptyList();

    /**
     * 支持的序列化算法
     */
    private List<Byte> serializationAlgorithms = Collections.emptyList();

    /**
     * 支持的压缩算法
     */
    private List<Byte> compressionAlgorithms = Collections.emptyList();


    public NegotiationInfo() {
    }
}
