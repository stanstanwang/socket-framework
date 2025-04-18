package com.zeewain.cbb.netty.util;

import cn.hutool.core.util.URLUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * @author stan
 * @date 2024/9/10
 */
public class NetUtilTest {


    @Test
    public void testParseUrl(){
        String url = "/abc/efg";
        URL url2 = URLUtil.url(url);
        System.out.println(url2);
        System.out.println(url2.getHost());
        System.out.println("end");
    }

    @Test
    public void testGetPath() {
        String url = "wss://dev.local.zeewain.com:443/api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc";
        String path = NetUtil.getPath(url);
        Assertions.assertEquals("/api/wsproxy/websocket?upstream=baiyun-lake-service-netty-nacos&roomId=abc", path);
    }

}