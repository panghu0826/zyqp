package com.guosen.webx.web;

import com.guosen.webx.util.PropHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Properties;

public class ServerJetty {
    public static Properties config;

    private static Server server;
    private static VertxAdapterHandler handler;

    private static final String DEFAULT_CONFIG_FILE = "config/server.properties";

    public static void main(String[] args) throws Exception {
        String configFile = DEFAULT_CONFIG_FILE;
        if (args.length > 0) {
            configFile = args[0];
        }
        System.out.println("load file " + configFile);

        config = PropHelper.loadProperties(configFile, PropHelper.SCOPE_ABS);

        int defaultPort = 8888;
        String port = config.getProperty("port");
        if (port != null)
            defaultPort = Integer.parseInt(port);

        server = new Server(defaultPort);
        handler = new VertxAdapterHandler(config);
        server.setHandler(handler);

        QueuedThreadPool threadPool = new QueuedThreadPool();
        setPool(threadPool, config);
        server.setThreadPool(threadPool);

        server.setDumpAfterStart(true);
        server.setDumpBeforeStop(true);

        server.start();
        server.join();
    }

    public synchronized static void stop() {
        if (handler != null) {
            try {
                handler.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (server != null) {
            try {
                server.stop();
                Thread.currentThread().sleep(1000 * 3);
                System.exit(-1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private static void setPool(QueuedThreadPool threadPool, Properties config) {
        String maxThreads = config.getProperty("maxThreads");
        if (maxThreads != null)
            threadPool.setMaxThreads(Integer.valueOf(maxThreads));

        String minThreads = config.getProperty("minThreads");
        if (minThreads != null)
            threadPool.setMinThreads(Integer.valueOf(minThreads));
    }
}
