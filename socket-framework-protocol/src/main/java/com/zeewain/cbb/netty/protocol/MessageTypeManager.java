package com.zeewain.cbb.netty.protocol;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * 放的是 msgCode 到入参的类型信息， msgCode 到出参的类型信息 rpc 自己处理
 */
public class MessageTypeManager {
    private static final Map<String, Type> MSG_TYPE_MAP = new HashMap<>(32);

    public static Type find(String msgCode) {
        return MSG_TYPE_MAP.get(msgCode);
    }

    public static void register(String msgCode, Type type) {
        MSG_TYPE_MAP.put(msgCode, type);
    }
}