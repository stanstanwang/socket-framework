package com.zeewain.socket.netty.core.lifecycle;

import com.alibaba.fastjson.JSON;
import com.zeewain.socket.core.server.ServerConnectionListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author stan
 * @date 2024/9/12
 */
@Component
public class WebsocketConnectionListener implements ServerConnectionListener {

    @Nullable
    @Override
    public HttpResponse onWsConnected(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        if (httpRequest.uri().contains("403")) {
            return ServerConnectionListener.constructResp(httpRequest, HttpResponseStatus.FORBIDDEN);
        } else if (httpRequest.uri().contains("401")) {
            Map<String,Object> map = new HashMap<>();
            map.put("code", 401);
            map.put("msg", "未授权");
            byte[] jsonBytes = JSON.toJSONBytes(map);
            return ServerConnectionListener.constructResp(httpRequest, HttpResponseStatus.FORBIDDEN, jsonBytes);
        }
        return null;
    }
}
