package com.oj.ide.complier.enums;


/**
 * author: wm
 * date:  2017/9/26
 * desc: 请求结果ENUM
 */
public enum ResultTypeEnum {
    ok(200), fail(400), error(500);
    private Integer typeCode;

    ResultTypeEnum(Integer code) {
        this.typeCode = code;
    }

    public Integer getTypeCode() {
        return typeCode;
    }
}
