package com.zeewain.cbb.netty.protocol.base;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jdk.nashorn.internal.runtime.Context;
import lombok.Getter;

import java.nio.BufferOverflowException;


/**
 * 返回状态码枚举
 * @author zwl, GLF
 * @version 2021年03月04日
 **/
public enum StatusCodeEnum {

    SUCCESS(0, "成功"),

    ERROR(1, "系统异常");


    @Getter
    @JsonValue
    public final int code;

    @JSONCreator
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatusCodeEnum codeOf(int code) {
        for (StatusCodeEnum item :
                StatusCodeEnum.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        throw new RuntimeException("返回状态码枚举类型不存在, type=" + code);
    }

    @Getter
    public final String message;

    StatusCodeEnum(int code, String desc) {

        this.code = code;
        this.message = desc;
    }
}
