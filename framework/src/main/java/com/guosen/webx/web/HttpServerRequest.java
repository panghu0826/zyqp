package com.guosen.webx.web;

import com.alibaba.fastjson.JSON;
import com.guosen.webx.util.JedisStore;
import groovy.lang.Closure;
import net.sourceforge.fastupload.FastUploadParser;
import net.sourceforge.fastupload.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class HttpServerRequest {
    public static final String ENCODING = "utf-8";
    public static final String SESSION_KEY = "web-session-";
    public static final String SESSION_ID = "sid";
    public static Map<String, Object> sessionMap = new HashMap<String, Object>();

    private HttpServletRequest req;
    private HttpServletResponse resp;

    private Logger log = LoggerFactory.getLogger(HttpServerRequest.class);

    public HttpServerRequest(HttpServletRequest request, HttpServletResponse response) {
        this.req = request;
        this.resp = response;
    }

    private Map<String, Object> locals = new HashMap<String, Object>();

    public void setLocal(String key, Object value) {
        this.locals.put(key, value);
    }

    public Object getLocal(String key) {
        return this.locals.get(key);
    }

    public String getPath() {
        return req.getRequestURI();
    }

    public String getUri() {
        String path = req.getRequestURI();
        String query = req.getQueryString();
        if (query != null)
            path += "?" + query;
        return path;
    }

    public String getAbsoluteURI() {
        return req.getRequestURI();
    }

    public String getMethod() {
        return req.getMethod();
    }

    public String getQuery() {
        return req.getQueryString();
    }

    public String getRemoteAddr() {
        return req.getRemoteAddr();
    }

    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
        if (headers != null)
            return headers;

        headers = new HashMap<String, String>();

        Enumeration names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement().toString();
            String val = req.getHeader(name);

            headers.put(name, val);
        }

        return headers;
    }

    private Map<String, String> p;

    public Map<String, String> getParams() {
        if (p != null)
            return p;

        p = new HashMap<String, String>();

        Map map = req.getParameterMap();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object v = entry.getValue();
            if (v != null) {
                if (v instanceof String[]) {
                    String[] arr = (String[]) v;
                    p.put(key, arr[0]);
                } else {
                    p.put(key, v.toString());
                }
            }
        }

        return p;
    }

    public boolean isAjax() {
        return req.getHeader("X-Requested-With") != null;
    }

    public String getCookie() {
        return getCookie(SESSION_ID);
    }

    public String getCookie(String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
            return null;

        for (Cookie c : cookies) {
            if (c.getName().equals(name))
                return c.getValue();
        }

        return null;
    }

    private byte[] readBytes(InputStream is) throws IOException {
        final int BUFFER_SIZE = 4 * 1024;
        byte[] buffer = new byte[BUFFER_SIZE];

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int length = 0;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }

        is.close();
        return os.toByteArray();
    }

    public void bodyHandler(Closure cl) {
        try {
            ServletInputStream is = req.getInputStream();
            byte[] bytes = readBytes(is);

            String body;
            if (bytes.length == 0)
                body = req.getParameterNames().nextElement().toString();
            else
                body = new String(bytes, ENCODING);

            cl.call(body);
        } catch (Exception e) {
            log.error("body handler error", e);
        }
    }

    public void jsonHandler(Closure cl) {
        try {
            ServletInputStream is = req.getInputStream();
            byte[] bytes = readBytes(is);

            String body;
            if (bytes.length == 0)
                body = req.getParameterNames().nextElement().toString();
            else
                body = new String(bytes, ENCODING);

            cl.call(JSON.parse(body));
        } catch (Exception e) {
            log.error("json handler error", e);
        }
    }

    public void endHandler(Closure cl) {
        cl.call();
    }

    Map<String, String> attr;

    public void uploadHandler(Closure cl) throws IOException {
        uploadHandler(true, cl);
    }

    public void uploadHandler(boolean readBytes, Closure cl) throws IOException {
        attr = new HashMap<String, String>();
        UploaderHelper upload = new UploaderHelper();

        req.setCharacterEncoding(ENCODING);
        FastUploadParser parser = new FastUploadParser(req);
        List<MultiPart> list = parser.parseList();
        for (MultiPart item : list) {
            if (item.isFile()) {
                upload.setFilename(item.getFileName());
                upload.setFieldname(item.getFieldName());
                if (readBytes)
                    upload.setBytes(item.getContentBuffer());
                else
                    upload.setInputStream(item.getInputStream());
            } else {
                try {
                    attr.put(item.getFieldName(), item.getString(ENCODING));
                } catch (Exception ex) {
                }
            }
        }

        cl.call(upload);
    }

    public Map<String, String> getFormAttributes() {
        return attr;
    }

    private String c(String key) {
        return ServerJetty.config.getProperty(key);
    }

    private String generateCookieRandom(int len) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        int baseLen = base.length();

        StringBuilder sb = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < len; i++) {
            int begin = rand.nextInt(baseLen - 1);
            sb.append(base.substring(begin, begin + 1));
        }

        return sb.toString();
    }

    public Object session(String key) {
        return session(key, null);
    }

    public Object session(String key, Object val) {
        return session(key, val, null);
    }

    public Object session(String key, Object val, String cookieVal) {
        String cookieSessionId = getCookie();
        if (cookieVal != null || cookieSessionId == null) {
            cookieSessionId = cookieVal != null ? cookieVal : generateCookieRandom(32);
            Cookie cookie = new Cookie(SESSION_ID, cookieSessionId);
            cookie.setPath("/");
            cookie.setSecure(false);

            // default 1 hour, need a pre filter to refresh cookie max age
            int defaultMaxAge = 60 * 60;
            String cookieMaxAge = c("cookieMaxAge");
            if (cookieMaxAge != null) {
                defaultMaxAge = Integer.parseInt(cookieMaxAge);
            }
            cookie.setMaxAge(defaultMaxAge);
            resp.addCookie(cookie);
        }

        //JedisStore inst = JedisStore.getInstance();
        if (val != null) {
            //inst.set(SESSION_KEY + cookieSessionId, key, JSON.toJSONString(val));
            sessionMap.put(SESSION_KEY + cookieSessionId+key, JSON.toJSONString(val));
            return val;
        } else {
            //String json = inst.get(SESSION_KEY + cookieSessionId, key);
            String json = (String) sessionMap.get(SESSION_KEY + cookieSessionId+key);
            return json == null ? json : JSON.parse(json);
        }
    }

    public void clearSession() {
        String cookieSessionId = getCookie();
        if (cookieSessionId == null) {
            return;
        }

        //JedisStore inst = JedisStore.getInstance();
        //inst.clear(SESSION_KEY + cookieSessionId);
        sessionMap.clear();
    }

    public void delSession(String key) {
        String cookieSessionId = getCookie();
        if (cookieSessionId == null) {
            return;
        }

        Cookie cookie = new Cookie(SESSION_ID, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(false);
        resp.addCookie(cookie);

        JedisStore inst = JedisStore.getInstance();
        inst.del(SESSION_KEY + cookieSessionId, key);
    }
}
