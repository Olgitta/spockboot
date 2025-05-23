package com.olg.redis.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private final ValueOperations<String, String> valueOps;
    private final HashOperations<String, String, String> hashOps;

    public RedisService(RedisTemplate<String, String> redisTemplate,
                        StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.hashOps = redisTemplate.opsForHash();
    }

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    public void set(String key, Duration timeout) {
        set(key, "1", timeout);  // default value
    }

    public boolean setIfAbsent(String key, Duration timeout) {
        // SET key value NX EX
        return Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(key, "1", timeout)
        );
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void hset(String hashKey, String field, String value) {
        stringRedisTemplate.opsForHash().put(hashKey, field, value);
    }

    public String hget(String hashKey, String field) {
        Object result = stringRedisTemplate.opsForHash().get(hashKey, field);
        return result != null ? result.toString() : null;
    }

    public Map<String, String> hgetall(String hashKey) {
        return hashOps.entries(hashKey);
    }

    public void hmset(String hashKey, Map<String, String> payload) {
        stringRedisTemplate.opsForHash().putAll(hashKey, payload);
    }

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public void hdel(String hashKey, String field) {
        stringRedisTemplate.opsForHash().delete(hashKey, field);
    }

    public String executeLuaScript(
            String luaScript,
            List<String> keys,
            List<String> args) {

        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(String.class);

        return stringRedisTemplate.execute(script, keys, args.toArray());
    }

    /// ///////////////////////////////

//    // SET key value
//    public void set(String key, Object value) {
//        valueOps.set(key, value);
//    }

//    // GET key
//    public Object get(String key) {
//        return valueOps.get(key);
//    }

//    // HSET hashKey field value
//    public void hset(String hashKey, Object field, Object value) {
//        hashOps.put(hashKey, field, value);
//    }
//
//    // HGET hashKey field
//    public Object hget(String hashKey, Object field) {
//        return hashOps.get(hashKey, field);
//    }

//    // PUBLISH to channel
//    public void publish(String channel, String message) {
//        stringRedisTemplate.convertAndSend(channel, message);
//    }
}


