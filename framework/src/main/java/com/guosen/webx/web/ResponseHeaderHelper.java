package com.guosen.webx.web;

import javax.servlet.http.HttpServletResponse;

public class ResponseHeaderHelper {

    private HttpServletResponse resp;

    public ResponseHeaderHelper(HttpServletResponse response) {
        this.resp = response;
    }

    public void set(String name, String val) {
        resp.setHeader(name, val);
    }
}
