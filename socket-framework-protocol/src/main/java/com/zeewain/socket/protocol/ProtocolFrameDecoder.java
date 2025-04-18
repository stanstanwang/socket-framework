package com.zeewain.socket.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


/**
 * 基于当前通用协议定义的粘包拆包处理器
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {


    public ProtocolFrameDecoder() {
        this(ProtocolConstants.MAX_FRAME_LENGTH);
    }


    public ProtocolFrameDecoder(int maxFrameLength) {
        /*
        int maxFrameLength,
        int lengthFieldOffset,  magic code is 4B, and version is 1B, and then FullLength. so value is 5
        int lengthFieldLength,  FullLength is int(4B). so values is 4
        int lengthAdjustment,   FullLength include all data and read 9 bytes before, so the left length is (FullLength-9). so values is -9
        int initialBytesToStrip we will check magic code and version self, so do not strip any bytes. so values is 0
        */
        super(maxFrameLength, 5, 4, -9, 0);
    }
}
