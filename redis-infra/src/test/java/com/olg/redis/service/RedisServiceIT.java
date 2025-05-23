package com.olg.redis.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {RedisService.class})
@ContextConfiguration(classes = RedisTestConfiguration.class)
//@TestPropertySource(locations = "classpath:application-test.properties")
public class RedisServiceIT {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisService redisService;

    @Test
    public void testOps() {
        redisService.set("k", "v");
        assertEquals("v", redisService.get("k"));

        redisService.hset("myset", "f", "v");
        redisService.hset("myset", "x", "y");

        assertEquals("y", redisService.hget("myset", "x"));
        Map<String, String> mySet = redisService.hgetall("myset");

//        System.out.println(mySet);

        assertTrue(mySet.containsKey("f"));
    }

    @Test
    public void testLuaScript() {
        String hashKey = "hashKey";
        String channelName = "channelName";
        String field = "field";
        String lockKey = "lockKey";
        String timestamp = Instant.now().toString();
        String LUA_LOCK_SCRIPT =
                "local set = redis.call('SETNX', KEYS[1], '1')\n" +
                        "if set == 0 then return {err = 'KEY EXISTS'} end\n" +
                        "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +
                        "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2])\n" +
                        "redis.call('PUBLISH', KEYS[4], ARGV[3])\n" +
                        "return 'OK'";

        try {
            String result = redisService.executeLuaScript(
                    LUA_LOCK_SCRIPT,
                    Arrays.asList(lockKey, hashKey, field, channelName),
                    Arrays.asList("600", timestamp, "channel_message")
            );

            assertEquals("OK", result);

            redisService.executeLuaScript(
                    LUA_LOCK_SCRIPT,
                    Arrays.asList(lockKey, hashKey, field, channelName),
                    Arrays.asList("600", timestamp, "channel_message")
            );

        } catch (Exception e) {
            assertEquals("KEY EXISTS", e.getCause().getMessage());
        }

//        assertNotEquals("OK", result);
    }

//    // Your integration test cases using redisTemplate
//    @Test
//    public void testRedisOperations() {
//        // Example: Perform Redis operations and assert results
//        redisTemplate.opsForValue().set("key", "value");
//        String result = (String) redisTemplate.opsForValue().get("key");
////        assertThat(result).isEqualTo("value");
//    }
}