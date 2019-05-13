package com.guosen.webx.web;

//for groovy closure compile
public interface ErrorCaller {
    public void call(HttpServerRequest req, HttpServerResponse resp, Exception ex);
}
