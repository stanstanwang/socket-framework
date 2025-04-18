package com.zeewain.socket.netty.rpc;

import io.netty.util.concurrent.Promise;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author stan
 * @date 2022/8/9
 */
public class ClassTest {


    @Test
    public void testEncode() {
        List<BasicNameValuePair> list = Arrays.asList(
                new BasicNameValuePair("username", "nacos"),
                new BasicNameValuePair("password", "zeewain@123"));
        String format = URLEncodedUtils.format(list, "UTF-8");
        System.out.println(format);
    }

    @Test
    public void test() {
        System.out.println(Number.class.isInstance(3.0)); // true
        System.out.println(Integer.class.isInstance(3.0)); // false

        System.out.println(Future.class.isAssignableFrom(Promise.class)); // true
        System.out.println(Promise.class.isAssignableFrom(Future.class)); // false
    }


    @SneakyThrows
    public byte[] testDivideZero() {
        return "abc".getBytes("efg");
    }

    @Test
    public void test2() {
        System.out.println(testDivideZero());
    }


}
