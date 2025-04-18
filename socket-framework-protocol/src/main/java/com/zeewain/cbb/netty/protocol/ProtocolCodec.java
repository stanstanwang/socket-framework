package com.zeewain.cbb.netty.protocol;

import com.zeewain.cbb.netty.protocol.codec.BodyCodec;
import com.zeewain.cbb.netty.protocol.compress.Compressor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static com.zeewain.cbb.netty.protocol.ProtocolConstants.*;


/**
 * 协议的编解码，关于协议的定义
 * 查看 <a href="https://bookstack.local.zeewain.com/books/352fe/page/04025#bkmrk-%E5%8D%8F%E8%AE%AE%E5%AE%9A%E4%B9%89">参考</a>
 */
@Slf4j
public class ProtocolCodec extends ByteToMessageCodec<Message> {

    private final byte[] EMPTY_ARRAY = new byte[0];

    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        NegotiationInfo negotiationInfo = NegotiationContext.getNegotiationInfo(ctx.channel());

        // 4 字节的魔数
        out.writeBytes(MAGIC_CODE_BYTES);
        // 1 字节的版本
        out.writeByte(VERSION);

        // 4 字节总长度字段，先留空，后边再回填总长度
        int totalLengthFieldIndex = out.writerIndex();
        out.writeInt(0);

        // 1 字节的序列化方式 jdk 0 , json 1, 其他待扩展
        byte codec = determineCodec(msg.getCodec(), msg.getBody(), msg.getProduces(), negotiationInfo.getSerializationAlgorithms());
        msg.setCodec(codec);
        out.writeByte(codec);
        // 1 字节的压缩算法， 先定义为0， 写完之后回填
        int compressIndex = out.writerIndex();
        out.writeByte(0);

        // 4 个字节的请求序号
        out.writeInt(msg.getId());
        // 1 字节预留字段标记位
        byte flags = 0;
        flags = (byte) (flags | (msg.isRequest() ? 0 : IS_RESPONSE));
        out.writeByte(flags);

        // msgType
        byte[] msgType = msg.getMsgCode().getBytes(DEFAULT_CHARSET);
        byte msgTypeLength = (byte) msgType.length;
        out.writeByte(msgTypeLength);
        out.writeBytes(msgType);

        // header
        // 先定义为0，写完之后回填
        int headLengthFieldIndex = out.writerIndex();
        out.writeShort(0);

        Map<String, String> headMap = msg.getHeadMap();
        short headLength = 0;
        if (headMap != null && !headMap.isEmpty()) {
            headLength = (short) HeadMapSerializer.getInstance().encode(headMap, out);
        }

        // body, body 应该放前边，这样很多字节都好处理
        byte[] body = encodeBodyWithError(codec, msg.getBody());
        Compressor compressor = getCompressor(body.length, negotiationInfo.getCompressionAlgorithms());
        body = compressor.compress(body);
        int bodyLength = body.length;
        out.writeInt(bodyLength);
        out.writeBytes(body);

        // 对一些值做回填
        int currentWriteIndex = out.writerIndex();
        // 回填 totalLength 和 headLength, 这里除了长度本身，还会有长度标志位的byte大小
        int totalLength = msgTypeLength + 1 + headLength + 2 + bodyLength + 4
                + V1_HEAD_LENGTH;
        out.writerIndex(totalLengthFieldIndex).writeInt(totalLength);
        out.writerIndex(headLengthFieldIndex).writeShort(headLength);
        out.writerIndex(compressIndex).writeByte(compressor.getType());
        out.writerIndex(currentWriteIndex);
    }


    private static Compressor getCompressor(int bodyLength, List<Byte> compressionAlgorithms) {
        return Compressor.getCompressor(compressionAlgorithms, bodyLength);
    }


    /**
     * 根据上下文和消息体确定要选择的编码
     *
     * @param codec                   消息上指定的序列化协议
     * @param body                    当前的数据类型
     * @param produces                定义的响应数据类型
     * @param serializationAlgorithms 客户端支持的响应类型
     */
    private static Byte determineCodec(Byte codec, Object body, byte[] produces, List<Byte> serializationAlgorithms) {
        if (codec != null) {
            return codec;
        }


        // 1. 判断期望响应的格式和协商的协议
        // 这个值可以从注解取，可以结果取， 结果不支持默认就json
        if (produces != null && produces.length == 1) {
            return produces[0];
        }

        // 从两个协议中取交集，重新算出一个新的协议

        // 2. 从支持的协议中选择一个
        return BodyCodec.getCodec(serializationAlgorithms, body)
                .getType();
    }


    /**
     * 序列化 body 且加上异常提醒
     *
     * @param code 编解码器
     * @param body 对应body
     * @return 编解码成功的字节数组
     */
    private byte[] encodeBodyWithError(byte code, Object body) {
        // 这里是对响应体或者发送的请求进行编解码， 所以结果未必是 NettyResponse
        try {
            BodyCodec codec = BodyCodec.getByType(code);
            if (codec == null) {
                throw new NullPointerException(String.format("编解码器code=%s不存在", code));
            }
            return codec.encode(body);
        } catch (Exception e) {
            log.error(String.format("encode数据异常, 数据%s", body), e);
            return EMPTY_ARRAY;
        }
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 4 字节的魔术
        checkMagicCode(in);

        // 1 字节的版本号
        byte version = in.readByte();

        // 4 字节的总长度
        int totalLength = in.readInt();

        Message message = new Message();

        // 1 字节的序列化和反序列化
        byte codecType = in.readByte();
        message.setCodec(codecType);

        // 1 字节的压缩算法
        byte compressorType = in.readByte();
        message.setCompressor(compressorType);

        // 4 字节的id
        int id = in.readInt();
        message.setId(id);
        // 1 字节的预留长度
        byte flags = in.readByte();
        // 当前消息是否是响应的结果
        boolean isResponse = (IS_RESPONSE & flags) == IS_RESPONSE;
        message.setRequest(!isResponse);

        // 1 字节的 msgType
        byte msgTypeLength = in.readByte();
        byte[] msgTypeByte = new byte[msgTypeLength];
        in.readBytes(msgTypeByte);
        String msgType = new String(msgTypeByte, DEFAULT_CHARSET);
        message.setMsgCode(msgType);

        // 2 字节的 head
        short headShort = in.readShort();
        if (headShort > 0) {
            Map<String, String> headMap = HeadMapSerializer.getInstance().decode(in, headShort);
            message.getHeadMap().putAll(headMap);
        }

        // 4 字节的的body
        int bodyLength = in.readInt();
        byte[] bodyByte = new byte[bodyLength];
        if (bodyLength > 0) {
            in.readBytes(bodyByte);
            if (compressorType != Compressor.UN_COMPRESS) {
                bodyByte = Compressor.getByType(compressorType).uncompress(bodyByte);
            }
        }

        // 请求的话是直接解析，响应的话是用到再解析
        Type type = MessageTypeManager.find(msgType);
        if (message.isRequest() && type != null) {
            try {
                Object body = BodyCodec.getByType(codecType)
                        .decode(bodyByte, type);
                message.setBody(body);
            } catch (Exception e) {
                log.warn("decode body error, msgType={}, errorMsg={}",
                        msgType, e.getMessage());
                throw e;
            }
        } else {
            // 后续再反序列化，目前是响应值这种根据 @NettyProcessor 解析不出来，只能 @NettyClient 执行的时候再反序列化
            message.setBody(bodyByte);
        }
        out.add(message);
    }


    /**
     * 判断魔数是否匹配
     */
    private void checkMagicCode(ByteBuf in) {
        byte[] magicCodes = new byte[4];
        in.readBytes(magicCodes);

        if (!matchMagicCode(magicCodes)) {
            // fix stan 2022/8/4 魔术错误连接直接关闭
            throw new IllegalArgumentException(String.format("Unknown magic code: %s", new String(magicCodes, DEFAULT_CHARSET)));
        }
    }


}