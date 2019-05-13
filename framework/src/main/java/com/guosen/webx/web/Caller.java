package com.guosen.webx.web;

import com.guosen.webx.beans.Context;

// for groovy closure compile
public interface Caller {
    public Object call(HttpServerRequest req, HttpServerResponse resp, Context ctx);
}
