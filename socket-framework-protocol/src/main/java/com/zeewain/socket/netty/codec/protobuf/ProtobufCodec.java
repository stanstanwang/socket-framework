package com.zeewain.socket.netty.codec.protobuf;

import cn.hutool.core.util.TypeUtil;
import com.google.protobuf.Any;
import com.google.protobuf.Message;
import com.zeewain.socket.netty.NettyProto;
import com.zeewain.socket.netty.NettyResponse;
import com.zeewain.socket.netty.codec.BodyCodec;
import com.zeewain.socket.netty.util.InterfaceUtil;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * json 实现的序列化和反序列化, 后续可抽象接口不同实现
 *
 * @author stan
 * @description
 * @date 2022/7/20
 */
public class ProtobufCodec implements BodyCodec {


    private static final byte[] EMPTY = new byte[0];

    public ProtobufCodec() {
        BodyCodec.register(getType(), this);
    }


    /**
     * 判断当前编码器是否支持该类型, 目前只用在 encode 上， 比如发数据和响应数据的时候， 另外 decode 是根据别人传输的数据过来的。
     * 判断逻辑是看入参是不是 pb 类型， 或者有实现 pb 转换器 {@link ProtobufConverter}, 这些都是 pb 类型。
     * 如果是 resp 的话， 会判断到 data 的具体类型。
     *
     * @param msg 类型
     */
    @Override
    public boolean support(Object msg) {
        if (msg == null) {
            return false;
        }

        // 如果想要封装类型这种，需要特殊的额外处理了
        if (msg instanceof NettyResponse) {
            NettyResponse<?> resp = (NettyResponse<?>) msg;
            // resp 默认空值也使用 protobuf，只要调整这里便可
            // 之前 json 的话消息体是 25 bytes, 换成 pb 是 2 bytes
            // 不过因为影响范围比较广，这个先响应 false 忽略
            if (resp.getData() == null) {
                // 后续根据成功或失败来？
                // 失败的方式不太好控制，因为可能失败本身定义的就是 pb 格式
                return true;
            } else if (ProtobufConvertUtil.isProtobufType(getProtoType(resp.getData().getClass()))) {
                return true;
            }
        }

        return ProtobufConvertUtil.isProtobufType(getProtoType(msg.getClass()));
    }/*
    有两个小问题记录下；
    1. 如果是pb类型的话，异常信息 data 为 null， 这种优先使用 pb
    2. 如果是void类型的话，走这里都会响应pb， 因为协议协商上都支持pb，所以这样响应还可以接受
    3. heartbeat 和 negotiation 都是 json 类型，因为属于内置的， json 类型处理方便
    */


    @Override
    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unchecked"})
    public byte[] encode(Object msg) {
        if (msg == null) {
            return EMPTY;
        }

        /*
         * 处理 resp 响应的问题， 这里必须转换为 protobuf 的 NettyResponse
         */
        if (msg instanceof NettyResponse) {
            NettyResponse<?> resp = (NettyResponse<?>) msg;
            NettyProto.NettyResponse.Builder builder = NettyProto.NettyResponse.newBuilder()
                    .setCode(resp.getCode()).setSuccess(resp.isSuccess());
            if (resp.getMessage() != null) {
                builder.setMessage(resp.getMessage());
            }
            Object data = resp.getData();
            if (data != null) {
                if (ProtobufConvertUtil.isProtobufInstance(data)) {
                    builder.setData(Any.pack((Message) data));
                } else if (ProtobufConvertUtil.isConverterInstance(data)) {
                    Class<Message> protoType = getProtoType(data.getClass());
                    builder.setData(Any.pack(ProtobufConvertUtil.convertToProtobufByCode((ProtobufConverter) data, protoType)));
                } else {
                    throw new IllegalArgumentException(String.format("unexpected protobuf type %s", data.getClass()));
                }
            }
            return builder.build().toByteArray();
        }

        /*
         * 否则就是直接的 protobuf 类型， NettyProto.NettyResponse 也在这里处理
         */
        if (ProtobufConvertUtil.isProtobufInstance(msg)) {
            return ((Message) msg).toByteArray();
        }


        /*
         * 处理 @ProtobufType 这种情况
         */
        if (ProtobufConvertUtil.isConverterInstance(msg)) {
            Class<Message> protoType = getProtoType(msg.getClass());
            return ProtobufConvertUtil.convertToProtobufByCode((ProtobufConverter) msg, protoType).toByteArray();
        }

        throw new IllegalArgumentException(String.format("output type must be protobuf, but found %s", msg.getClass()));
    }


    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) {
            bytes = EMPTY;
        }
        // 空数组的话也要构造出默认对象，因为默认值也是空数组的情况
        Message.Builder builder = ProtobufConvertUtil.getMessageBuilder(clazz);
        builder.mergeFrom(bytes);
        return (T) builder.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <T> T decode(byte[] bytes, Type type) {
        // 进来这里 decode 的话必然是 protobuf 类型，这个信息是放在 message.codec 上的。
        if (bytes == null || bytes.length == 0) {
            bytes = EMPTY;
        }

        Class<?> clazz = TypeUtil.getClass(type);

        /*
         * 处理 resp 响应的问题， 这个属于与业务有关的一些耦合了
         */
        if (NettyProto.NettyResponse.class.isAssignableFrom(clazz)) {
            // 这个只能业务自己解析里边的泛型数据
            return (T) NettyProto.NettyResponse.parseFrom(bytes);
        } else if (NettyResponse.class.isAssignableFrom(clazz)) {
            // 处理 NettyResponse<ProtobufType>
            NettyProto.NettyResponse protoResp = NettyProto.NettyResponse.parseFrom(bytes);
            NettyResponse<Object> resp = NettyResponse.fromProtobuf(protoResp);

            // 转换 data， protobuf 里边内容不空的时候才做转换
            if (!protoResp.getData().equals(Any.getDefaultInstance())) {
                Class<?> firstTypeArg = getFirstArgType(type);
                Class<Message> protoType = getProtoType(firstTypeArg);
                if (protoType == null) {
                    throw new IllegalArgumentException(String.format("cant' find prototype for %s", type));
                }
                Message data = protoResp.getData().unpack(protoType);

                // 如果是 prototype， 直接设置值， 否则得做下转换
                if (ProtobufConvertUtil.isProtobufType(firstTypeArg)) {
                    resp.setData(data);
                } else if (ProtobufConvertUtil.isConverterType(firstTypeArg)) {
                    resp.setData(ProtobufConvertUtil.convertFromProtobufByCode(data, firstTypeArg));
                } else {
                    throw new IllegalArgumentException(String.format("unexpected protobuf type %s", firstTypeArg));
                }
            }
            return (T) resp;
        }


        /*
         * 否则就是直接的 protobuf 类型
         * NettyProto.NettyResponse 其实也可以在这里处理, 考虑性能原因上边直接避免反射调用
         */
        if (ProtobufConvertUtil.isProtobufType(clazz)) {
            return (T) decode(bytes, clazz);
        }

        /*
         * 处理 @ProtobufType 这种情况
         */

        if (ProtobufConvertUtil.isConverterType(clazz)) {
            Class<Message> protoType = getProtoType(clazz);
            Message message = decode(bytes, protoType);
            return (T) ProtobufConvertUtil.convertFromProtobufByCode(message, clazz);
        }
        throw new IllegalArgumentException(String.format("output type must be protobuf, but found %s", type));
    }


    // 获取泛型的第一个参数类型
    public static Class<?> getFirstArgType(Type type) {
        return TypeUtil.getClass(TypeUtil.getTypeArgument(type));
    }

    /**
     * 获取 prototype 的具体类型
     *
     * @param type 支持直接类型，或者 NettyResponse 封装过的类型
     */
    @SuppressWarnings("unchecked")
    private static Class<Message> getProtoType(Type type) {
        Class<?> typeClazz = TypeUtil.getClass(type);
        Class<?> actualClass;

        // 处理 NettyResponse<ProtoType> 和 NettyResponse<CommonType>
        if (NettyResponse.class.isAssignableFrom(typeClazz)) {
            actualClass = getFirstArgType(type);
        } else {
            actualClass = typeClazz;
        }
        if (ProtobufConvertUtil.isProtobufType(actualClass)) {
            return (Class<Message>) actualClass;
        } else if (ProtobufConvertUtil.isConverterType(actualClass)) {
            Optional<Type> actualType = InterfaceUtil.getInterfaceFirstArgType(actualClass, ProtobufConverter.class);
            if (actualType.isPresent()) {
                return (Class<Message>) TypeUtil.getClass(actualType.get());
            } else {
                throw new IllegalArgumentException(String.format("ProtobufConverter 类型推断失败 %s", actualClass));
            }
        }
        return null;
    }


    @Override
    public byte getType() {
        return CODEC_TYPE_PROTOBUF;
    }


}
