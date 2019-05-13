package com.guosen.webx.util;

/**
 * Created by 卢丹文 on 2016/6/2.
 * 返回给前端的状态码
 */
public enum StatusCode {

    SUCCESS(0), FAIL(-1), NEED_LOGIN(-999), NEED_BIND(-1000), BIZ_ERROR(-10001),DUPLICATE_BIND(-10002),

    //需要第三方授权
    NEED_AUTHOR(-998);

    private int value;

    StatusCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }


}
