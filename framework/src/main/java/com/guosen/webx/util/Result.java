package com.guosen.webx.util;

import java.util.Map;

/**
 * Created by 卢丹文 on 2016/6/3.
 */
public class Result {
    int statusCode = StatusCode.FAIL.getValue();
    String msg;
    Map data;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    public static Result fail(int code, String msg) {
        Result res = new Result();
        res.statusCode = code;
        res.msg = msg;
        return res;
    }

    public static Result fail(int code) {
        return fail(code, "处理出错");
    }

    public static Result fail(String msg) {
        return fail(StatusCode.FAIL.getValue(), msg);
    }

    public static Result success() {
        Result res = new Result();
        res.statusCode = StatusCode.SUCCESS.getValue();
        return res;
    }

    public static Result success(Map map) {
        Result res = new Result();
        res.statusCode = StatusCode.SUCCESS.getValue();
        res.data = map;
        return res;
    }
}