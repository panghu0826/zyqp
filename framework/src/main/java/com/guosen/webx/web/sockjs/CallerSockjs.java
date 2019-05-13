package com.guosen.webx.web.sockjs;

import com.alibaba.fastjson.JSONObject;
import com.guosen.webx.beans.Context;

// for groovy closure compile
public interface CallerSockjs {
    public Object call(SockJSSocket sock, JSONObject obj, Context ctx);
}
