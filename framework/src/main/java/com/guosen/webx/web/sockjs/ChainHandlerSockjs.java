package com.guosen.webx.web.sockjs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guosen.webx.beans.Context;

// mock vertx sockjs
public class ChainHandlerSockjs {
    public Set<SockJSSocket> connections = new HashSet<SockJSSocket>();

    private final List<Router> routerList = new ArrayList<Router>();
    private CallerSockjs endHandler;

    private static final String ACTION = "action";

    public void add(String action, CallerSockjs handler) {
        if (action == null || handler == null)
            return;

        for (Router one : routerList) {
            if (action.equals(one.action)) {
                one.handler = handler;
                break;
            }
        }

        routerList.add(new Router(action, handler));
    }

    public void endHandle(CallerSockjs handler) {
        endHandler = handler;
    }

    public CallerSockjs getEndHandler() {
        return endHandler;
    }

    public Object handle(SockJSSocket sock, JSONObject obj) {
        if (obj == null)
            return null;

        String action = (String) obj.get(ACTION);
        if (action == null)
            return null;

        for (Router one : routerList) {
            if (action.equals(one.action)) {
                return one.handler.call(sock, obj, Context.getInst());
            }
        }

        return null;
    }

    public void broadcast(Object obj) {
        for (SockJSSocket one : connections) {
            one.leftShift(JSON.toJSONString(obj));
        }
    }

    public boolean send(String sockid, Object obj) {
        for (SockJSSocket one : connections) {
            if (one.getWriteHandlerID().equals(sockid)) {
                one.leftShift(JSON.toJSONString(obj));
                return true;
            }
        }
        return false;
    }

    private static class Router {
        public final String action;
        public CallerSockjs handler;

        public Router(String action, CallerSockjs handler) {
            this.action = action;
            this.handler = handler;
        }
    }
}
