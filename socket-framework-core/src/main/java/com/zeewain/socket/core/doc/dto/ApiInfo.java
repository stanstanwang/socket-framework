package com.zeewain.socket.core.doc.dto;

import lombok.Data;

import javax.annotation.Nullable;

/**
 * @author stan
 * @date 2022/8/23
 */
@Data
public class ApiInfo {

    /**
     * 组信息
     */
    private String group;

    /**
     * 接口名称
     */
    private String name;


    /**
     * 路径
     */
    private String path;

    /**
     * 描述信息
     */
    private String desc;


    /**
     * 接口参数
     */
    private @Nullable TypeInfo parameter;

    /**
     * 接口响应结果
     */
    private @Nullable TypeInfo result;

}
