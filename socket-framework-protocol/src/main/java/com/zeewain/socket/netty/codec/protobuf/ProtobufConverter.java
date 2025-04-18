package com.zeewain.socket.netty.codec.protobuf;

import com.google.protobuf.Message;

/**
 *
 * WARNING, protobuf 里边，是没有null值的，
 * 字符串默认为"", 数字默认为0或者0.0， 转过来java对象的话，需要特别注意。
 * 1. 可能有一个字段status是0的情况，通过json转成java变成了null，是因为0 protobuf 当做了null。
 * <a href="https://zhuanlan.zhihu.com/p/46603988">protobuf空值的问题呢</a>
 *
 * @author stan
 * @date 2023/4/21
 */
public interface ProtobufConverter<T extends Message> {

    default T convertToProtobuf(Class<T> type) {
        return ProtobufConvertUtil.convertToProtobufByJson(this, type);
    }

    default Object convertFromProtobuf(T src) {
        return ProtobufConvertUtil.convertFromProtobufByJson(src, this.getClass());
    }

}
