package comp.common

import com.alibaba.fastjson.JSON
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

/**
 * 操作Redis的Template设计模式的封装，用闭包方法作为参数
 * Created by kerry on 2016/6/20.
 */
//@Component
class RedisCacher {
    Logger log = LoggerFactory.getLogger(RedisCacher.class)

    @Autowired
    JedisPool jedisPool

    /**
     * 使用jedis的方式，不用每次都finally
     * @param cl
     */
    public void doSth(Closure cl) {
        Jedis jedis = jedisPool.resource
        try {
            cl.call(jedis)
        } finally {
            jedisPool.returnResource(jedis)
        }
    }

    /**
     * 用redis做简单的缓存
     * @param key 缓存的key
     * @param expired 缓存时效，毫秒
     * @param cl 回调方法，如果缓存中的对象不存在或已经失效，就把这个回调方法的返回值重新缓存起来
     * @return
     */
    public Map doAndCache(String key, int expired, Closure cl) {
        Map r
        doSth { Jedis jedis ->
            String str = jedis.get(key)
            if (str != null) {
                log.info('use cache ' + key)
                r = JSON.parse(str)
                return
            }

            r = cl.call()
            str = JSON.toJSONString(r)
            jedis.setex(key, expired, str)
        }
        r
    }
}
