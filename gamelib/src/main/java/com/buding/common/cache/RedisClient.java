package com.buding.common.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

/**
 * redis命令参考手册：
 * http://doc.redisfans.com/index.html
 * @author Administrator
 *
 */
public interface RedisClient {
    public void disconnect();

    /**
     * 设置单个值
     * 
     * @param key
     * @param value
     * @return
     */
    public String set(String key, String value);

    /**
     * 获取单个值
     * 
     * @param key
     * @return
     */
    public String get(String key);

    public Boolean exists(String key);

    public String type(String key);

    /**
     * 在某段时间后失效
     * 
     * @param key
     * @param unixTime
     * @return
     */
    public Long expire(String key, int seconds);
    
    public Long pexpire(String key, long milliseconds);

    /**
     * 在某个时间点失效
     * 
     * @param key
     * @param unixTime
     * @return
     */
    public Long expireAt(String key, long unixTime);
    
    public Long pexpireAt(String key, long millisecondsTimestamp);

    public Long ttl(String key);
    
    public Long pttl(String key);

    public boolean setbit(String key, long offset, boolean value);

    public boolean getbit(String key, long offset);

    public long setrange(String key, long offset, String value);

    public String getrange(String key, long startOffset, long endOffset);

    public String getSet(String key, String value) ;

    public Long setnx(String key, String value) ;

    public String setex(String key, int seconds, String value) ;

    public Long decrBy(String key, long integer) ;

    public Long decr(String key) ;

    public Long incrBy(String key, long integer) ;

    public Long incr(String key) ;

    public Long append(String key, String value) ;

    public String substr(String key, int start, int end) ;

    public Long hset(String key, String field, String value) ;

    public String hget(String key, String field) ;

    public Long hsetnx(String key, String field, String value) ;

    public String hmset(String key, Map<String, String> hash) ;

    public List<String> hmget(String key, String... fields) ;

    public Long hincrBy(String key, String field, long value) ;

    public Boolean hexists(String key, String field) ;

    public Long del(String key) ;

    public Long hdel(String key, String field) ;

    public Long hlen(String key) ;

    public Set<String> hkeys(String key) ;

    public List<String> hvals(String key) ;

    public Map<String, String> hgetAll(String key) ;

    // ================list ====== l表示 list或 left, r表示right====================
    public Long rpush(String key, String string) ;

    public Long lpush(String key, String string) ;

    public Long llen(String key) ;

    public List<String> lrange(String key, long start, long end) ;

    public String ltrim(String key, long start, long end) ;

    public String lindex(String key, long index) ;

    public String lset(String key, long index, String value) ;

    public Long lrem(String key, long count, String value) ;

    public String lpop(String key) ;
    
    public List<String> blpop(String key) ;

    public String rpop(String key) ;

    //return 1 add a not exist value ,
    //return 0 add a exist value
    public Long sadd(String key, String member) ;

    public Set<String> smembers(String key) ;

    public Long srem(String key, String member) ;

    public String spop(String key) ;

    public Long scard(String key) ;

    public Boolean sismember(String key, String member) ;

    public String srandmember(String key) ;

    public Long zadd(String key, double score, String member) ;

    public Set<String> zrange(String key, int start, int end) ;

    public Long zrem(String key, String member) ;

    public Double zincrby(String key, double score, String member) ;

    public Long zrank(String key, String member) ;

    public Long zrevrank(String key, String member) ;

    public Set<String> zrevrange(String key, int start, int end) ;

    public Set<Tuple> zrangeWithScores(String key, int start, int end) ;

    public Set<Tuple> zrevrangeWithScores(String key, int start, int end) ;

    public Long zcard(String key) ;

    public Double zscore(String key, String member) ;

    public List<String> sort(String key) ;

    public List<String> sort(String key, SortingParams sortingParameters) ;

    public Long zcount(String key, double min, double max) ;

    public Set<String> zrangeByScore(String key, double min, double max) ;

    public Set<String> zrevrangeByScore(String key, double max, double min) ;

    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) ;

    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) ;

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) ;

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) ;

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) ;

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) ;

    public Long zremrangeByRank(String key, int start, int end) ;

    public Long zremrangeByScore(String key, double start, double end) ;

    public Long linsert(String key, LIST_POSITION where, String pivot, String value) ;

    public String set(byte[] key, byte[] value) ;
    
    public String set(String key, byte[] value) ;

    public byte[] get(byte[] key) ;

    public Boolean exists(byte[] key) ;

    public String type(byte[] key) ;

    public Long expire(byte[] key, int seconds) ;
    
    public Long pexpire(byte[] key, long milliseconds) ;

    public Long expireAt(byte[] key, long unixTime) ;
    
    public Long pexpireAt(byte[] key, long millisecondsTimestamp) ;

    public Long ttl(byte[] key) ;
   
    public byte[] getSet(byte[] key, byte[] value) ;

    public Long setnx(byte[] key, byte[] value) ;

    public String setex(byte[] key, int seconds, byte[] value) ;

    public Long decrBy(byte[] key, long integer) ;

    public Long decr(byte[] key) ;

    public Long incrBy(byte[] key, long integer) ;

    public Long incr(byte[] key) ;

    public Long append(byte[] key, byte[] value) ;

    public byte[] substr(byte[] key, int start, int end) ;

    public Long hset(byte[] key, byte[] field, byte[] value) ;

    public byte[] hget(byte[] key, byte[] field) ;

    public Long hsetnx(byte[] key, byte[] field, byte[] value) ;

    public String hmset(byte[] key, Map<byte[], byte[]> hash) ;

    public List<byte[]> hmget(byte[] key, byte[]... fields) ;

    public Long hincrBy(byte[] key, byte[] field, long value) ;

    public Boolean hexists(byte[] key, byte[] field) ;

    public Long hdel(byte[] key, byte[] field) ;

    public Long hlen(byte[] key) ;

    public Set<byte[]> hkeys(byte[] key) ;

    public Collection<byte[]> hvals(byte[] key) ;

    public Map<byte[], byte[]> hgetAll(byte[] key) ;

    public Long rpush(byte[] key, byte[] string) ;

    public Long lpush(byte[] key, byte[] string) ;

    public Long llen(byte[] key) ;

    public List<byte[]> lrange(byte[] key, int start, int end) ;

    public String ltrim(byte[] key, int start, int end) ;

    public byte[] lindex(byte[] key, int index) ;

    public String lset(byte[] key, int index, byte[] value) ;

    public Long lrem(byte[] key, int count, byte[] value) ;

    public byte[] lpop(byte[] key) ;

    public byte[] rpop(byte[] key) ;

    public Long sadd(byte[] key, byte[] member) ;

    public Set<byte[]> smembers(byte[] key) ;

    public Long srem(byte[] key, byte[] member) ;

    public byte[] spop(byte[] key) ;

    public Long scard(byte[] key) ;

    public Boolean sismember(byte[] key, byte[] member) ;

    public byte[] srandmember(byte[] key) ;

    public Long zadd(byte[] key, double score, byte[] member) ;

    public Set<byte[]> zrange(byte[] key, int start, int end) ;

    public Long zrem(byte[] key, byte[] member) ;

    public Double zincrby(byte[] key, double score, byte[] member) ;

    public Long zrank(byte[] key, byte[] member) ;

    public Long zrevrank(byte[] key, byte[] member) ;

    public Set<byte[]> zrevrange(byte[] key, int start, int end) ;

    public Set<Tuple> zrangeWithScores(byte[] key, int start, int end) ;

    public Set<Tuple> zrevrangeWithScores(byte[] key, int start, int end) ;

    public Long zcard(byte[] key) ;

    public Double zscore(byte[] key, byte[] member) ;

    public List<byte[]> sort(byte[] key) ;

    public List<byte[]> sort(byte[] key, SortingParams sortingParameters) ;

    public Long zcount(byte[] key, double min, double max) ;

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max) ;

    public Set<byte[]> zrangeByScore(byte[] key, double min, double max, int offset, int count) ;

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max) ;

    public Set<Tuple> zrangeByScoreWithScores(byte[] key, double min, double max, int offset, int count) ;

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min) ;

    public Set<byte[]> zrevrangeByScore(byte[] key, double max, double min, int offset, int count) ;

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min) ;

    public Set<Tuple> zrevrangeByScoreWithScores(byte[] key, double max, double min, int offset, int count) ;

    public Long zremrangeByRank(byte[] key, int start, int end) ;

    public Long zremrangeByScore(byte[] key, double start, double end) ;

    public Long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) ;

    public List<Object> pipelined(ShardedJedisPipeline shardedJedisPipeline) ;

    public Jedis getShard(byte[] key) ;

    public Jedis getShard(String key) ;

    public JedisShardInfo getShardInfo(byte[] key) ;

    public JedisShardInfo getShardInfo(String key) ;

    public String getKeyTag(String key) ;

    public Collection<JedisShardInfo> getAllShardInfo() ;

    public Collection<Jedis> getAllShards() ;

}
