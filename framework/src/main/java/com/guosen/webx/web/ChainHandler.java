package com.guosen.webx.web;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guosen.webx.beans.Context;

// http request router
public class ChainHandler {
    public final static String PRE = "pre";
    public final static String NEXT = "next";
    public final static String RESULT = "route_result";

    private ErrorCaller errorHandler = null;
    private Caller noMatchHandler = null;

    private ChainEventListener eventListener = null;

    public ChainEventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(ChainEventListener eventListener) {
        this.eventListener = eventListener;
    }

    private Logger log = LoggerFactory.getLogger(ChainHandler.class);

    private final LinkedList<Interceptor> interceptorList = new LinkedList<Interceptor>();
    private final List<Router> routerList = new ArrayList<Router>();

    public List<Router> getRouterList() {
        return routerList;
    }

    public ChainHandler setErrorHandler(ErrorCaller closure) {
        errorHandler = closure;
        return this;
    }

    public ChainHandler setNoMatchHandler(Caller closure) {
        noMatchHandler = closure;
        return this;
    }

    private Router getOne(String method, String urlPat) {
        for (Router one : routerList) {
            if (method.equals(one.method) && urlPat.equals(one.urlPat))
                return one;
        }

        return null;
    }

    public ChainHandler addRouter(String method, String urlPat, Caller handler) {
        // no repeat, replace instead
        Router one = getOne(method, urlPat);
        if (one != null) {
            one.handler = handler;
            return this;
        }

        // /book/:bookId, router parameter
        Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(urlPat);
        StringBuffer sb = new StringBuffer();
        Set<String> groups = new HashSet<String>();
        while (m.find()) {
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException(
                        "Cannot use identifier " + group + " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<$1>[^\\/]+)");
            groups.add(group);
        }
        m.appendTail(sb);
        String regex = sb.toString();
        Pattern pat = Pattern.compile(regex);
        Router router = new Router(method, urlPat, pat, groups, handler);
        routerList.add(router);
        if (eventListener != null) {
            eventListener.addRouter(method, urlPat, pat);
        }

        return this;
    }

    public ChainHandler addRouterRegEx(String method, String urlPat, Caller handler) {
        // no repeat, replace instead
        Router one = getOne(method, urlPat);
        if (one != null) {
            one.handler = handler;
            return this;
        }

        Pattern pat = Pattern.compile(urlPat);
        Router router = new Router(method, urlPat, pat, null, handler);
        routerList.add(router);
        if (eventListener != null) {
            eventListener.addRouter(method, urlPat, pat);
        }
        return this;
    }

    // for short
    public ChainHandler get(String urlPat, Caller handler) {
        this.addRouter("get", urlPat, handler);
        return this;
    }

    public ChainHandler post(String urlPat, Caller handler) {
        this.addRouter("post", urlPat, handler);
        return this;
    }

    public ChainHandler getRegEx(String urlPat, Caller handler) {
        this.addRouterRegEx("get", urlPat, handler);
        return this;
    }

    public ChainHandler postRegEx(String urlPat, Caller handler) {
        this.addRouterRegEx("post", urlPat, handler);
        return this;
    }

    public ChainHandler filter(String name, String type, String urlPat, Caller handler) {
        return this.filter(name, type, urlPat, 1, handler);
    }

    public ChainHandler filter(String name, String type, String urlPat, int pos, Caller handler) {
        if (!PRE.equals(type) && !NEXT.equals(type)) {
            throw new IllegalArgumentException("Type should be pre or next!");
        }

        // no repeat, replace instead
        Pattern pat = Pattern.compile(urlPat);
        for (Interceptor one : interceptorList) {
            if (name.equals(one.name) && type.equals(one.type)) {
                one.pattern = pat;
                one.handler = handler;
                return this;
            }
        }

        Interceptor newer = new Interceptor(name, type, pat, pos, handler);
        interceptorList.add(newer);
        Collections.sort(interceptorList, new Comparator<Interceptor>() {
            @Override
            public int compare(Interceptor t1, Interceptor t2) {
                return t1.pos - t2.pos;
            }
        });

        if (eventListener != null) {
            eventListener.addFilter(name, type, urlPat, pat);
        }

        return this;
    }

    private void route(HttpServerRequest req, HttpServerResponse resp) {
        String method = req.getMethod();
        for (Router one : routerList) {
            if (!method.equalsIgnoreCase(one.method))
                continue;

            Matcher m = one.pattern.matcher(req.getPath());
            if (!m.matches())
                continue;

            Map<String, String> reqParams = req.getParams();
            if (one.groups != null) {
                for (String param : one.groups) {
                    reqParams.put(param, m.group(param));
                }
            } else {
                for (int i = 0; i < m.groupCount(); ++i) {
                    reqParams.put("param" + i, m.group(i + 1));
                }
            }

            Object r = one.handler.call(req, resp, Context.getInst());
            if (r != null)
                req.setLocal(RESULT, r);

            return;
        }

        // no match
        if (this.noMatchHandler != null) {
            this.noMatchHandler.call(req, resp, Context.getInst());
        } else {
            resp.setStatusCode(404).end();
        }
    }

    private boolean matchUrl(String url, Pattern pat) {
        return pat.matcher(url).matches();
    }

    private String concatStr(String... strs) {
        StringBuilder sb = new StringBuilder();

        for (String str : strs) {
            sb.append(str);
        }

        return sb.toString();
    }

    private String concatLog(String name, String url, boolean isBegin) {
        return concatStr("Profile pre ", name, " for ", url, " ", (isBegin ? " begin " : " end "),
                "" + System.currentTimeMillis());
    }

    public void handle(HttpServerRequest req, HttpServerResponse resp) {
        try {
            String url = req.getPath();
            if (checkInterceptor(req, resp, url, PRE)) return;

            if (log.isDebugEnabled()) {
                log.debug(concatLog("main route", url, true));
            }
            route(req, resp);
            if (log.isDebugEnabled()) {
                log.debug(concatLog("main route", url, false));
            }

            if (checkInterceptor(req, resp, url, NEXT)) return;
        } catch (Exception ex) {
            log.error("Exception caught when hand request!", ex);
            if (errorHandler != null)
                errorHandler.call(req, resp, ex);
        }
    }

    private boolean checkInterceptor(HttpServerRequest req, HttpServerResponse resp, String url, String filterType) {
        for (Interceptor one : interceptorList) {
            if (!filterType.equals(one.type) || !matchUrl(url, one.pattern))
                continue;

            if (log.isDebugEnabled()) {
                log.debug(concatLog(one.name, url, true));
            }
            boolean isOk = one.handle(req, resp);
            if (log.isDebugEnabled()) {
                log.debug(concatLog(one.name, url, false));
            }
            if (isOk)
                return true;
        }
        return false;
    }

    // **** **** **** **** **** ****
    static class Router {
        public final String method;
        public final String urlPat;
        public final Pattern pattern;
        public final Set<String> groups;
        public Caller handler;

        public Router(String method, String urlPat, Pattern pattern, Set<String> groups, Caller handler) {
            this.method = method;
            this.urlPat = urlPat;
            this.pattern = pattern;
            this.groups = groups;
            this.handler = handler;
        }
    }

    static class Interceptor {
        final String name;
        final String type;
        Pattern pattern;
        Caller handler;
        int pos;

        public Interceptor(String name, String type, Pattern pattern, int pos, Caller handler) {
            this.name = name;
            this.type = type;
            this.pattern = pattern;
            this.handler = handler;
            this.pos = pos;
        }

        // true means break and return
        public boolean handle(HttpServerRequest req, HttpServerResponse resp) {
            Boolean r = (Boolean) handler.call(req, resp, Context.getInst());
            return r == null ? false : r.booleanValue();
        }
    }
}
