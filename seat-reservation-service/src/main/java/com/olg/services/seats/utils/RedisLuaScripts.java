package com.olg.services.seats.utils;

public class RedisLuaScripts {
    private RedisLuaScripts() {
        // Utility class, prevent instantiation
    }

    public static String LUA_LOCK_SCRIPT =
            "local set = redis.call('SETNX', KEYS[1], '1')\n" + // Set the lock key
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
