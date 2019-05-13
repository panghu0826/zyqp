package com.guosen.webx.web;

import com.guosen.webx.beans.ClassLoaderHolder;
import com.guosen.webx.beans.Context;
import com.guosen.webx.web.sockjs.ChainHandlerSockjs;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class VertxAdapterHandler extends AbstractHandler {
    private Logger log = LoggerFactory.getLogger(VertxAdapterHandler.class);

    private Timer t;

    private final static String ENCODING = "utf-8";
    private final static String SRC_DIR = "src";

    private Properties config;

    private ChainHandler handler = new ChainHandler();

    // handlers/components last modified time
    private Map<String, Long> modified = new HashMap<String, Long>();

    public VertxAdapterHandler(Properties config) {
        this.config = config;
        log.info("config - " + config);

        init();
        initContext();
    }

    private void initContext() {
        Context.getInst();
        log.info("context init..");
        Smarty4jRender.getInst().config(config);
    }

    private void init() {
        CompilerConfiguration compileConf = new CompilerConfiguration();
        compileConf.setSourceEncoding(ENCODING);
        compileConf.setClasspath(SRC_DIR);

        Binding binding = new Binding();
        binding.setProperty("log", log);
        binding.setProperty("config", config);
        binding.setProperty("handler", handler);
        // just adapt
        binding.setProperty("handlerSockjs", new ChainHandlerSockjs());

        final GroovyShell shell = new GroovyShell(ClassLoaderHolder.gcl, binding, compileConf);

        final ReferParser refer = new ReferParser();

        TimerTask task = new TimerTask() {
            private Logger log = LoggerFactory.getLogger(this.getClass());
            private List<String> changedCompList = new ArrayList<String>();

            private String isReferCompChanged(File f) {
                for (String changedComp : changedCompList) {
                    if (refer.isHandlerReferComp(changedComp, f.getAbsolutePath()))
                        return changedComp;
                }
                return null;
            }

            private void evalFile(File f) {
                if (f.isDirectory()) {
                    File[] files = f.listFiles();
                    for (File sub : files) {
                        // recursive
                        evalFile(sub);
                    }
                } else if (f.isFile() && f.getName().endsWith(".groovy")) {
                    String path = f.getAbsolutePath();
                    Long t = modified.get(path);

                    String changedReferComp = isReferCompChanged(f);
                    if (changedReferComp != null)
                        log.info("refer comp has changed - " + f.getAbsolutePath() + " - " + changedReferComp);
                    if (t == null || t.longValue() != f.lastModified() || changedReferComp != null) {
                        modified.put(path, f.lastModified());

                        // if is handler, compile again
                        if (path.contains("handler")) {
                            try {
                                shell.evaluate(f);
                                log.info("handler-binding compile ok 4 - " + f.getName());

                                ChainEventListener eventListener = handler.getEventListener();
                                if (eventListener != null) {
                                    eventListener.reset();
                                    log.info("handler-event-listener : " + eventListener);
                                }

                                refer.parse(f);
                            } catch (Exception e) {
                                log.error("handler-binding error", e);
                            }
                        }
                    }
                }
            }

            private void checkReloadContext(File d) {
                changedCompList.clear();

                @SuppressWarnings("rawtypes")
                Iterator it = FileUtils.iterateFiles(d, new String[]{"groovy"}, true);
                boolean isChanged = false;

                while (it.hasNext()) {
                    File f = (File) it.next();
                    String path = f.getAbsolutePath();
                    if (!path.contains("comp")) {
                        continue;
                    }

                    Long t = modified.get(path);
                    if (t == null) {
                        modified.put(path, f.lastModified());
                    } else if (t.longValue() != f.lastModified()) {
                        modified.put(path, f.lastModified());

                        log.info("component file changed - " + f.getName());
                        isChanged = true;

                        int len = f.getName().length() - ".groovy".length();
                        changedCompList.add(f.getName().substring(0, len));
                    }
                }

                if (isChanged) {
                    Context.getInst().reload();
                }
            }

            @Override
            public void run() {
                File d = new File(SRC_DIR);
                if (!d.exists())
                    return;

                // first reload beans, so the handler will compile correct
                checkReloadContext(d);
                evalFile(d);
            }

        };

        // execute init file only once
        final String initFileName = "Init.groovy";
        try {
            shell.evaluate(new File(SRC_DIR + "/" + initFileName));
        } catch (IOException e) {
            log.error("evaluate init file fail!", e);
        }

        final long defaultInterval = 1000 * 10;
        String scheduleInterval = config.getProperty("handlerLoadInterval");
        long interval = scheduleInterval != null ? Long.valueOf(scheduleInterval) : defaultInterval;

        t = new Timer();
        t.schedule(task, new Date(), interval);
    }

    public void close() {
        Context.getInst().close();
        ClassLoaderHolder.gcl.clearCache();

        if (t != null)
            t.cancel();
    }

    @Override
    public void handle(String target, Request base, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        handler.handle(new HttpServerRequest(req, resp), new HttpServerResponse(resp));
    }
}
