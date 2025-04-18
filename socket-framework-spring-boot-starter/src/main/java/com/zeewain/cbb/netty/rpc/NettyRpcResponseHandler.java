package com.zeewain.cbb.netty.rpc;

import cn.hutool.core.thread.NamedThreadFactory;
import com.zeewain.cbb.netty.protocol.Message;
import com.zeewain.cbb.netty.util.CollectionUtils;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * rpc调用的时候，这里会维护callback的关系， 等rpc回来的时候， 唤醒等待的 callback 去执行
 *
 * @author stan
 * @date 2022/7/27
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyRpcResponseHandler extends SimpleChannelInboundHandler<Message> {

    // 客户端纬度的 channelId 可能会重复，所以这个得做隔离, 通过隔离的方式减少线程并发访问的资源占用问题
    private static final Map<ChannelId, Map<Integer, RpcPromise<Object>>> PROMISES = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService timerExecutor = new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("rpc-promise-checker", true));

    public static void put(Channel channel, int id, RpcPromise<Object> promise) {
        ChannelId channelId = channel.id();
        CollectionUtils.computeIfAbsent(PROMISES, channelId, cid -> new ConcurrentHashMap<>());
        PROMISES.get(channelId).put(id, promise);
    }

    public static RpcPromise<Object> remove(Channel channel, int id) {
        ChannelId channelId = channel.id();
        Map<Integer, RpcPromise<Object>> promiseMap = PROMISES.get(channelId);
        if(promiseMap != null) {
            return promiseMap.remove(id);
        }
        return null;
    }

    /**
     * channel 关闭的时候，清理掉关联的所有 promise
     */
    public static void remove(Channel channel) {
        Map<Integer, RpcPromise<Object>> unfinished = PROMISES.remove(channel.id());
        if (unfinished != null && unfinished.size() > 0) {
            log.warn("channel {} unfinished promise size {}", channel, unfinished.size());
        }
    }


    // 定时器检查， 将超时未响应的 promise 给置为失败状态
    // 这个跟业务上强制等待的效果有点一样
    static {
        timerExecutor.scheduleAtFixedRate(() -> {
            // log.debug("started execute promise timeout check");
            for (Map.Entry<ChannelId, Map<Integer, RpcPromise<Object>>> channelMap : PROMISES.entrySet()) {
                Iterator<Map.Entry<Integer, RpcPromise<Object>>> itr = channelMap.getValue().entrySet().iterator();
                while (itr.hasNext()) {
                    Map.Entry<Integer, RpcPromise<Object>> entry = itr.next();
                    RpcPromise<Object> promise = entry.getValue();
                    if (!promise.isDone() && promise.isTimeout()) {
                        itr.remove();
                        // 可能并发问题已经设置过了， 这里可能会设置失败
                        try {
                            promise.setTimeout();
                            log.warn("timeout clear promise: id={},msgCode={}", promise.getId(), promise.getMsgCode());
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
            }
        }, 60, 3, TimeUnit.SECONDS);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        // 我们这里只需要处理 resp 的情况，将 resp 的 promise 设置为 success
        if (msg.isRequest()) {
            ctx.fireChannelRead(msg);
            return;
        }

        RpcPromise<Object> promise = remove(ctx.channel(), msg.getId());
        if (promise == null) {
            log.warn("处理响应异常 id={},msgCode={} 找不初始请求", msg.getId(), msg.getMsgCode());
            // 错误的请求，就不需要透传了
            // ctx.fireChannelRead(msg);
            return;
        }

        log.debug("rpc发送请求-响应，msgId={}, msgCode={}, elapseTime={}ms",
                msg.getId(), msg.getMsgCode(), promise.getElapse());

        promise.setSuccess(msg);
    }
}
