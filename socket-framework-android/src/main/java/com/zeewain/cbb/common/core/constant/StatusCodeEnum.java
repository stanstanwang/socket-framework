package com.zeewain.cbb.common.core.constant;

public enum StatusCodeEnum {

    SUCCESS(0, "成功"),

    ERROR(1, "系统异常"),

    BASE_ERROR(1000, "通用基础业务异常"),

    HTTP_MESSAGE_NOT_READABLE(1001, "请求数据内容格式不匹配"),

    HTTP_MAXUPLOADSIZE_EXCEEDED(1002, "请求大小超出限制"),

    @Deprecated
    INVALID_PARAMETERS(1011, "请求参数校验异常"),

    PARAMETERS_VALIDATION(1011, "请求参数校验异常"),

    PARAMETERS_MISSING(1012, "请求参数缺失"),

    PARAMETERS_MISMATCH(1013, "请求参数字段不匹配/转换失败"),

    PARAMETERS_NOTVALID(1014, "请求参数匹配失败"),

    USER_NOT_LOGIN(1100, "用户未登录"),

    ACCOUNT_CANCELLED(1101, "账户已注销"),

    LOGIN_HAS_EXPIRED(1102, "TOKEN已过期"),

    TOKEN_ILLEGAL(1103, "TOKEN不合法"),

    TOKEN_CANCELLED(1104, "TOKEN注销"),

    ID_EXHAUSTED(1110, "id resource exhaustion"),

    //算法错误码定义2000-2999
    ALGORITHM_ERROR(2000, "算法异常"),

    //API错误码范围在3000-3200之间
    API_ERROR(3000, "API接口异常"),
    FEIGN_ERROR(3001, "微服务内部调用异常"),

    //限流熔断错误码范围在3200-3300之间
    SENTINEL_BLOCK(3200, "接口拦截阻塞"),
    SENTINEL_FLOW(3201, "接口服务限流"),
    SENTINEL_DEGRADE(3202, "接口服务降级"),
    SENTINEL_PARAM_FLOW(3203, "接口热点参数限流"),
    SENTINEL_SYSTEM_BLOCK(3204, "系统规则保护拦截"),
    SENTINEL_AUTHORITY(3205, "授权规则不通过拦截"),

    //权限错误码范围在4000-4100之间
    PERMISSION_ERROR(4000, "权限异常"),

    //业务错误码范围在10000以上
    BUSINESS_ERROR(10000, "业务处理异常");

    public static StatusCodeEnum codeOf(int code) {
        for (StatusCodeEnum item :
                StatusCodeEnum.values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        // ThrowCommonError("返回状态码枚举类型不存在, type={}", code);
        return null;
    }



    public final int code;
    public final String message;

    StatusCodeEnum(int code, String desc) {
        this.code = code;
        this.message = desc;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
