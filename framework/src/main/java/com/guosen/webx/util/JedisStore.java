package com.guosen.webx.util;

import com.guosen.webx.web.ServerJetty;
import redis.clients.jedis.Jedis;

public class JedisStore {
    private static Object lock = new Object();

    private static JedisStore instance;

    private JedisStore() {
        this.host = ServerJetty.config.getProperty("redisHost");
        client = new Jedis(host);
    }

    private String host;

    public static JedisStore getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null)
                    instance = new JedisStore();
            }
        }
        return instance;
    }

    private Jedis client;

    // http://www.tuicool.com/articles/beUF7v2
    public String get(String key, String name) {
        try {
            return client.hget(key, name);
        } catch (Exception e) {
            try {
                client.disconnect();
            } catch (Exception ex) {
            }
            this.client = new Jedis(host);
            return client.hget(key, name);
        }
    }

    public void set(String key, String name, String val) {
        try {
            client.hset(key, name, val);
        } catch (Exception e) {
            try {
                client.disconnect();
            } catch (Exception ex) {
            }
            this.client = new Jedis(host);
            client.hset(key, name, val);
        }
    }

    public void del(String key, String name) {
        try {
            client.hdel(key, new String[]{name});
        } catch (Exception e) {
            try {
                client.disconnect();
            } catch (Exception ex) {
            }
            this.client = new Jedis(host);
            client.hdel(key, new String[]{name});
        }
    }

    public void clear(String key) {
        try {
            client.del(key);
        } catch (Exception e) {
            try {
                client.disconnect();
            } catch (Exception ex) {
            }
            this.client = new Jedis(host);
            client.del(key);
        }
    }
}
