package com.guosen.webx.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.alibaba.fastjson.serializer.SerializeConfig;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.guosen.webx.util.Result;
import com.guosen.webx.util.StatusCode;
import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class HttpServerResponse {

    private HttpServletResponse resp;
    private static final SerializeConfig config;

    static {
        config = new SerializeConfig();
    }

    static final SerializerFeature[] features = {
            SerializerFeature.WriteMapNullValue, // 输出空置字段
            SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
            SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
            SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
            SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
    };

    public HttpServerResponse(HttpServletResponse response) {
        this.resp = response;
    }

    public void sendFile(String file) throws IOException {
        File f = new File(file);
        if (f.exists()) {
            FileInputStream is = new FileInputStream(f);
            end(is);
            is.close();
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public ResponseHeaderHelper getHeaders() {
        return new ResponseHeaderHelper(resp);
    }

    public HttpServerResponse setStatusCode(int statusCode) {
        resp.setStatus(statusCode);
        return this;
    }

    public HttpServerResponse addCookie(Map<String, String> p) {
        Cookie cookie = new Cookie(p.get("name"), p.get("value"));
        String path = p.get("path");
        if (path != null)
            cookie.setPath(path);
        String maxAge = p.get("maxAge");
        if (maxAge != null) {
            cookie.setMaxAge(Integer.valueOf(maxAge));
        }
        resp.addCookie(cookie);
        return this;
    }

    public void end() {
    }

    public void end(String str) throws IOException {
        byte[] bytes = str.getBytes(HttpServerRequest.ENCODING);
        resp.setContentLength(bytes.length);

        end(bytes);
    }

    public void end(byte[] bytes) throws IOException {
        ServletOutputStream os = resp.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }

    // input stream is not close
    public void end(InputStream is) throws IOException {
        ServletOutputStream os = resp.getOutputStream();
        IOUtils.copy(is, os);
        os.flush();
        os.close();
    }

    public void json(Object obj) throws IOException {
        resp.setContentType("application/json;charset=" + HttpServerRequest.ENCODING);
        end(JSON.toJSONString(obj, config, features));
    }

    private static final String DATA_KEY = "data";
    private static final String CODE_KEY = "statusCode";
    private static final String MSG_KEY = "message";

    // 与前端约定的数据格式
    public void jsonWrap(Object obj, int code, String msg) throws IOException {
        JSONObject r = new JSONObject();
        r.put(DATA_KEY, obj);
        r.put(CODE_KEY, code);
        if (msg != null) {
            r.put(MSG_KEY, msg);
        }
        json(r);
    }

    // 与前端约定的数据格式
    public void renderData(Result result) throws IOException {
        Map data = result.getData();
        String msg = result.getMsg();
        Integer statusCode = result.getStatusCode();
        jsonWrap(data, statusCode, msg);
    }

    // ok or fail
    public void ok(Object obj) throws IOException {
        jsonWrap(obj, StatusCode.SUCCESS.getValue(), null);
    }

    public void ok() throws IOException {
        jsonWrap(null, StatusCode.SUCCESS.getValue(), null);
    }

    public void fail(int code, String msg) throws IOException {
        jsonWrap(null, code, msg);
    }

    public void fail(StatusCode code, String msg) throws IOException {
        jsonWrap(null, code.getValue(), msg);
    }

    public void fail(String msg) throws IOException {
        jsonWrap(null, -1, msg);
    }

    public void fail(Object obj, String msg) throws IOException {
        jsonWrap(obj, -1, msg);
    }

    public void fail(StatusCode code, Object obj, String msg) throws IOException {
        jsonWrap(obj, code.getValue(), msg);
    }

    public void render(String tpl, Map<String, Object> data) throws Exception {
        resp.setContentType("text/html;charset=" + HttpServerRequest.ENCODING);
        Smarty4jRender.getInst().render(tpl, data, resp.getOutputStream());
    }

    public void redirect(String url) throws IOException {
        resp.sendRedirect(url);
    }
}
