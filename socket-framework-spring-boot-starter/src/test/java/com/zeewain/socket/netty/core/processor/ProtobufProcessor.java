package com.zeewain.socket.netty.core.processor;

import com.zeewain.socket.core.NettyMapping;
import com.zeewain.socket.core.NettyProcessor;
import com.zeewain.socket.netty.core.dto.Person;
import com.zeewain.socket.netty.core.dto.PersonProto;
import com.zeewain.socket.protocol.NettyResponse;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyProcessor
@NettyMapping("/protobuf/")
@RequiredArgsConstructor
@Slf4j
public class ProtobufProcessor {

    private final ProtobufClient protobufClient;


    @NettyMapping("req1")
    public NettyResponse<Person> req(ChannelHandlerContext ctx, Person person) {
        log.info("person req {}", person);
        person.setName("server " + System.currentTimeMillis());
        NettyResponse<Person> resp = protobufClient.notify(ctx.channel(), person);
        log.info("person resp {}", resp.getData());
        return NettyResponse.success(person);
    }

    @NettyMapping("req2")
    public NettyResponse<PersonProto.Person> req2(ChannelHandlerContext ctx, PersonProto.Person person) {
        log.info("person req {}", person);
        PersonProto.Person person2 = person.toBuilder()
                .setName("server " + System.currentTimeMillis()).build();
        NettyResponse<PersonProto.Person> resp = protobufClient.notify2(ctx.channel(), person2);
        log.info("person resp {}", resp.getData());
        return NettyResponse.success(person2);
    }


    @NettyMapping("emptyResp")
    public NettyResponse<Void> emptyResp(ChannelHandlerContext ctx, PersonProto.Person person) {
        return NettyResponse.success();
    }

    @NettyMapping("customResp")
    public PersonProto.Person customResp(ChannelHandlerContext ctx, PersonProto.Person person) {
        return person.toBuilder().setName("from server").build();
    }

    @NettyMapping("compress")
    public String compress(ChannelHandlerContext ctx, String str) {
        log.info("接受到字符串 长度={} 内容={}", str.length(), str);
        return str;
    }


}
