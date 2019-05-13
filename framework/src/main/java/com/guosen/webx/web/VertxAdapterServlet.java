package com.guosen.webx.web;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.handler.AbstractHandler;

import com.guosen.webx.util.PropHelper;

public class VertxAdapterServlet extends HttpServlet {
    private static final String CONF_FILE = "config/server.properties";

    AbstractHandler handler;

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handler.handle(null, null, req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String configPath = config.getInitParameter("configPath");
        if (configPath == null) {
            configPath = CONF_FILE;
        }
        Properties prop = PropHelper.loadProperties(CONF_FILE, PropHelper.SCOPE_ABS);
        handler = new VertxAdapterHandler(prop);
    }
}
