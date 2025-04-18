package com.zeewain.cbb.netty.doc;


import com.zeewain.cbb.netty.doc.dto.ApiInfo;

import java.util.List;

/**
 */
public interface DocRender {


    void render(List<ApiInfo> apis);


    /**
     * 根据type生成示例的值
     */
    static Object randomByType(String type) {
        if ("String".equals(type)) {
            return "xx";
        } else if ("Integer".equals(type) || "int".equals(type)) {
            return 123;
        } else if ("Boolean".equals(type) || "boolean".equals(type)) {
            return true;
        } else if ("Long".equals(type) || "long".equals(type)) {
            return 112L;
        } else if ("Float".equals(type) || "float".equals(type)) {
            return 1.2F;
        } else if ("Double".equals(type) || "double".equals(type)) {
            return 12.5D;
        } else if ("Date".equals(type) || "LocalDate".equals(type) || "LocalDateTime".equals(type)) {
            return "2022-10-11 09:00:00";
        }
        return "未识别的数据类型";
    }
}
