package com.guosen.webx.web.test;

import com.guosen.webx.util.PropHelper;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Properties;

public class ServerJettyTest {
    public static void main(String[] args) throws Exception {
        int defaultPort = 8888;

        Server server = new Server(defaultPort);
        server.setHandler(new TestHandler());

        String configFile = "config/server.properties";
        Properties config = PropHelper.loadProperties(configFile, PropHelper.SCOPE_ABS);

        QueuedThreadPool threadPool = new QueuedThreadPool();
        setPool(threadPool, config);
        server.setThreadPool(threadPool);

        server.start();
        server.join();
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
