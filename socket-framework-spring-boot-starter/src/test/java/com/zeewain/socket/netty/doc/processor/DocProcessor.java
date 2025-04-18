package com.zeewain.socket.netty.doc.processor;

import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.core.NettyProcessor;
import com.zeewain.socket.netty.core.dto.Foo;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyProcessor
@NettyMapping("aa/")
@Slf4j
@Api("aa")
public class DocProcessor {

    // 测试基本
    @NettyMapping("testObj")
    @ApiOperation(value = "1.testObj")
    public NettyResponse<Foo> testRespStr(Foo foo) {
        return NettyResponse.success("ok");
    }


    @NettyMapping("testBasic")
    @ApiOperation(value = "3.testBasic", notes = "响应值是流媒体地址")
    public NettyResponse<String> testRespStr(Long id) {
        return NettyResponse.success("ok");
    }

    @NettyMapping("testBasicList")
    @ApiOperation("3.testBasicList")
    public NettyResponse<List<String>> testBasicList(ChannelHandlerContext ctx, List<String> strs) {
        return null;
    }


    // 测试空
    @NettyMapping("testVoid")
    @ApiOperation("5.testVoid")
    public void testVoid(ChannelHandlerContext ctx) {
    }

    @NettyMapping("testVoidChannel")
    @ApiOperation("5.testVoidChannel")
    public void testChannel(Channel channel) {
    }
    @NettyMapping("testResp")
    @ApiOperation("5.testResp")
    public NettyResponse<Void> testResp() {
        return NettyResponse.success("ok");
    }

}
