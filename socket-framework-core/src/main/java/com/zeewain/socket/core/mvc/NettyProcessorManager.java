package com.zeewain.socket.core.mvc;

import cn.hutool.core.lang.Pair;
import com.zeewain.socket.core.Processor;
import com.zeewain.socket.netty.MessageTypeManager;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * 静态工具类，用于管理 Netty Processor
 *
 * @author stan
 * @date 2022/7/20
 */
public class NettyProcessorManager {


    private static final HashMap<String/*msgCode*/, Pair<Processor<?>, ExecutorService>> processorTable = new HashMap<>(32);


    /**
     * 注册进来
     *
     * @param msgCode   消息类型
     * @param processor 对应的解析器
     */
    public static void register(String msgCode, Processor<?> processor) {
        register(msgCode, processor, null);
    }


    /**
     * 注册进去
     *
     * @param msgCode         消息类型
     * @param processor       对应的解析器
     * @param executorService 线程执行器
     */

    public static void register(String msgCode, Processor<?> processor, ExecutorService executorService) {
        if (processorTable.containsKey(msgCode)) {
            throw new IllegalStateException(String.format("msgCode %s has multiple processor", msgCode));
        }
        processorTable.put(msgCode, Pair.of(processor, executorService));
        MessageTypeManager.register(msgCode, processor.getType());
    }

    /**
     * 根据消息类型找到对应的 Processor
     *
     * @param msgCode 消息类型
     */
    public static Pair<Processor<?>, ExecutorService> find(String msgCode) {
        return processorTable.get(msgCode);
    }


    /**
     * 判断当前是否已经注册了 Processor
     */
    public static boolean contains(String msgCode) {
        return processorTable.containsKey(msgCode);
    }


}
