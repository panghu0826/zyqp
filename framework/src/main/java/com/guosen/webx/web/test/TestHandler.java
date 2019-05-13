package com.guosen.webx.web.test;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * Created by kerry on 2016/5/18.
 */
public class TestHandler extends AbstractHandler {
    private static String str = "{\"success\":true,\"statusCode\":0,\"message\":\"\",\"data\":{\"data\":[{\"managerId\":96,\"managerName\":\"vince10\",\"managerNickname\":\"v8\",\"managerPassword\":\"9d8622060de5f24937b60585c3f4d66b\",\"managerRoleid\":3,\"managerPeopleid\":0,\"managerTime\":1444406400000,\"serviceId\":null,\"managerSummary\":null,\"style\":null,\"signature\":null,\"headImgUrl\":\"http://pay.qlogo.cn/mmopen/7XkeC9TeJ9urHCay4CzLPibj8UsfGxbiasnqqPqWr38ia7J4JoEoj9JXicibNmRoLQJhpIHEJyk4vicImfEWuzmI7I4bsGZh1YnuQ2/0\",\"focus\":false,\"follower\":null,\"privateManager\":false,\"curPage\":1,\"pagePreNum\":20,\"managerType\":0}],\"recordsTotal\":10}}";

    @Override
    public void handle(String s, Request request, HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Writer w = resp.getWriter();
        w.write(str);
        w.flush();
        w.close();
    }
}
