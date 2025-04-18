package com.zeewain.socket.netty.rpc;

import cn.hutool.core.lang.TypeReference;
import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.core.rpc.RpcMethod;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.util.concurrent.Promise;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * @author stan
 * @date 2022/8/2
 */

class NettyClientExecutorTest {

    @Test
    void invoke() {

    }


    /**
     * 测试解析方法体生成的对象
     */
    @Test
    void initRpcMethod() {
        RpcMethod rpcMethod = RpcMethod
                .fromMethod(findByName("testRpc"));
        Assertions.assertFalse(rpcMethod.isAsync());
        Assertions.assertEquals("testRpc", rpcMethod.getMsgCode());
        Assertions.assertEquals(void.class, rpcMethod.getReturnType());

        RpcMethod normalMethod = RpcMethod
                .fromMethod(findByName("testNormal"));
        Assertions.assertFalse(rpcMethod.isAsync());
        Assertions.assertEquals("testNormal", normalMethod.getMsgCode());
        Assertions.assertEquals(new TypeReference<NettyResponse<Void>>() {}.getType(), normalMethod.getReturnType());

        RpcMethod asyncMethod = RpcMethod
                .fromMethod(findByName("testAsync"));
        Assertions.assertTrue(asyncMethod.isAsync());
        Assertions.assertEquals("testAsync", asyncMethod.getMsgCode());
        Assertions.assertEquals(new TypeReference<NettyResponse<Void>>() {}.getType(), asyncMethod.getReturnType());

        RpcMethod asyncMethod2 = RpcMethod
                .fromMethod(findByName("testAsync2"));
        Assertions.assertTrue(asyncMethod2.isAsync());
        Assertions.assertEquals("testAsync2", asyncMethod2.getMsgCode());
        Assertions.assertEquals(new TypeReference<NettyResponse<Number>>() {}.getType(), asyncMethod2.getReturnType());
    }

    private Method findByName(String methodName){
        return Arrays.stream(NettyClientExecutorTest.class.getDeclaredMethods())
                .filter(it -> it.getName().equals(methodName))
                .findFirst().get();
    }

    @NettyMapping("testRpc")
    public void testRpc() {
    }

    @NettyMapping("testNormal")
    public NettyResponse<Void> testNormal() {
        return null;
    }

    @NettyMapping("testAsync")
    public Future<NettyResponse<Void>> testAsync() {
        return null;
    }

    @NettyMapping("testAsync2")
    public Promise<NettyResponse<Number>> testAsync2() {
        return null;
    }

}