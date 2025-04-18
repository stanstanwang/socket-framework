package com.zeewain.socket.protocol.compress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zeewain.socket.protocol.ProtocolConstants.COMPRESS_THRESHOLD;

/**
 * @author stan
 * @date 2023/4/19
 */
public interface Compressor {

    byte UN_COMPRESS = 0;
    byte GZIP_COMPRESS = 1;

    Map<Byte, Compressor> MAP_CACHE = new HashMap<>();

    /**
     * 压缩算法的类型
     */
    byte getType();

    byte[] compress(final byte[] src);

    byte[] uncompress(final byte[] src);


    static Compressor getCompressor(List<Byte> supportedAlgorithms, int byteLength) {
        if (supportedAlgorithms.contains(GZIP_COMPRESS)
                && byteLength > COMPRESS_THRESHOLD) {
            return getByType(GZIP_COMPRESS);
        }
        return getByType(UN_COMPRESS);
    }


    /**
     * 根据枚举获取对应的压缩器
     */
    static Compressor getByType(byte type) {
        return MAP_CACHE.get(type);
    }


    static void register(byte type, Compressor compressor) {
        MAP_CACHE.put(type, compressor);
    }

}
