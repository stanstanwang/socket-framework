package com.zeewain.cbb.netty.core.processor;

import com.zeewain.cbb.netty.core.NettyClient;
import com.zeewain.cbb.netty.core.NettyMapping;
import com.zeewain.cbb.netty.core.dto.Person;
import com.zeewain.cbb.netty.core.dto.PersonProto;
import com.zeewain.cbb.netty.protocol.NettyResponse;
import io.netty.channel.Channel;

/**
 * @author stan
 * @date 2022/8/26
 */
@NettyClient
@NettyMapping("/protobuf/")
public interface ProtobufClient {


    @NettyMapping("req1")
    NettyResponse<Person> req(Channel channel, Person person);


    @NettyMapping("notify1")
    NettyResponse<Person> notify(Channel channel, Person person);


    @NettyMapping("req2")
    NettyResponse<PersonProto.Person> req2(Channel channel, PersonProto.Person person);


    @NettyMapping("notify2")
    NettyResponse<PersonProto.Person> notify2(Channel channel, PersonProto.Person person);

    @NettyMapping("emptyResp")
    NettyResponse<Void> emptyResp(Channel channel, PersonProto.Person person);

    @NettyMapping("customResp")
    PersonProto.Person customResp(Channel channel, PersonProto.Person person);

    @NettyMapping("compress")
    String compress(Channel channel, String str);

}
