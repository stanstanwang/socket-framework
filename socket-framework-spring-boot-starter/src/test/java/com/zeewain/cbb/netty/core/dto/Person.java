package com.zeewain.cbb.netty.core.dto;

import com.zeewain.cbb.netty.protocol.codec.protobuf.ProtobufConverter;
import lombok.Data;

/**
 * @author stan
 * @date 2023/4/14
 */
@Data
public class Person implements ProtobufConverter<PersonProto.Person> {

    private Integer id;

    private String name;


    @Override
    public PersonProto.Person convertToProtobuf(Class<PersonProto.Person> type) {
        PersonProto.Person.Builder builder = PersonProto.Person.newBuilder();
        builder.setId(id).setName(name);
        return builder.build();
    }

    @Override
    public Object convertFromProtobuf(PersonProto.Person src) {
        this.setId(src.getId());
        this.setName(src.getName());
        return this;
    }
}
