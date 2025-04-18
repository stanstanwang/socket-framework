package com.zeewain.socket.core.doc.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 类型信息， 类型信息属于树形的。
 * 如果是基本类型， 那么就单层级
 * 如果对象，那么就树形。
 * 如果是 list， 那么就展开 list 的第一个为 树形。
 *
 * @author stan
 * @version 2022/8/22
 **/
@Data
@Accessors(chain = true)
public class TypeInfo implements Serializable, Cloneable {

    private String name;

    private String desc;

    private String type;

    /**
     * 默认值
     */
    private Object defaultValue;

    private Boolean required;


    private boolean basic = false;
    private boolean object = false;
    private boolean list = false;


    private List<TypeInfo> typeInfos = new ArrayList<>();


    public TypeInfo() {
    }


    public TypeInfo(String type) {
        this.type = type;
    }

    public TypeInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public TypeInfo(String name, String desc, String type, Object defaultValue, Boolean required) {
        this.name = name;
        this.desc = desc;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    @Override
    public TypeInfo clone(){
        try {
            return (TypeInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
