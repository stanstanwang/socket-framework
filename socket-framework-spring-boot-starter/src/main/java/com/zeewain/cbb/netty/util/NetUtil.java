package com.zeewain.cbb.netty.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * 内部使用的工具
 * @author stan
 * @date 2022/8/17
 */
public class NetUtil {


    /**
     * 获取路径后边的完整 path
     * @param url wss://dev.local.zeewain.com:443/api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc
     * @return /api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc
     */
    public static String getPath(String url) {
        int startIdx = -1;
        for (int i = 0; i < 3; i++) {
            startIdx = url.indexOf('/', startIdx + 1);
        }
        return url.substring(startIdx);
    }


    /**
     * To string address string.
     *
     * @param address the address
     * @return the string
     */
    public static String toStringAddress(SocketAddress address) {
        if (address == null) {
            return "";
        }
        // TODO delay stan 2022/9/5 直接用这个工具类，是不是比 toString 性能高
        return toStringAddress((InetSocketAddress) address);
    }

    public static String toStringAddress(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
}
