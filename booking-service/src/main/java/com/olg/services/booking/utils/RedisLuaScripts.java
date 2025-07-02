package com.olg.services.booking.utils;

public class RedisLuaScripts {
    private RedisLuaScripts() {
        // Utility class, prevent instantiation
    }
    /*
127.0.0.1:6379> keys *
1) "seat:lock:1_1_A_2"
2) "seat:lock:1_1_A_1"
3) "seats:reservation:1:1"
127.0.0.1:6379> get "seat:lock:1_1_A_2"
"abc"
127.0.0.1:6379> hgetall "seats:reservation:1:1"
1) "A_1"
2) "[\"2025-06-05T18:17:42.874269Z\",\"abc\"]"
3) "A_2"
4) "[\"2025-06-05T18:18:12.144156Z\",\"abc\"]"
127.0.0.1:6379>
    */
    public static String LUA_LOCK_SCRIPT =
            "local set = redis.call('SETNX', KEYS[1], ARGV[4])\n" + // Set the lock key
                    "if set == 0 then return {err = 'Seat already locked'} end\n" +
                    "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +  // Set the lock key ttl
                    "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2])\n" + // Set the seat into hash
                    "redis.call('PUBLISH', KEYS[4], ARGV[3])\n" +   // Publish lock event
                    "return 'OK'";

    public static String LUA_UNLOCK_SCRIPT =
            "redis.call('DEL', KEYS[1])\n" +                      // Delete the lock key
                    "redis.call('HDEL', KEYS[2], KEYS[3])\n" +           // Remove the seat from hash
                    "redis.call('PUBLISH', KEYS[4], ARGV[1])\n" +        // Publish unlock event
                    "return 'OK'";
}
