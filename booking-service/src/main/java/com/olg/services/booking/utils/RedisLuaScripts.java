package com.olg.services.booking.utils;

/**
 * Utility class containing Lua scripts for Redis operations related to seat locking and unlocking.
 * This class is designed to provide reusable Lua scripts for managing seat reservations in Redis.
 */
public class RedisLuaScripts {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private RedisLuaScripts() {
        // Utility class, prevent instantiation
    }

    /**
     * Lua script for locking a seat in Redis.
     * <p>
     * This script performs the following operations:
     * 1. Attempts to set a lock key using `SETNX` to ensure the seat is not already locked.
     * 2. Sets a time-to-live (TTL) for the lock key using `EXPIRE`.
     * 3. Adds the seat information to a Redis hash using `HSET`.
     * 4. Publishes a lock event to a Redis channel using `PUBLISH`.
     * </p>
     * <p>
     * Keys:
     * <ul>
     *     <li>KEYS[1]: Lock key for the seat.</li>
     *     <li>KEYS[2]: Hash key for seat reservations.</li>
     *     <li>KEYS[3]: Field in the hash representing the seat.</li>
     *     <li>KEYS[4]: Redis channel name for publishing lock events.</li>
     * </ul>
     * Arguments:
     * <ul>
     *     <li>ARGV[1]: TTL (time-to-live) for the lock key in seconds.</li>
     *     <li>ARGV[2]: Value to store in the hash for the seat.</li>
     *     <li>ARGV[3]: Message to publish to the Redis channel.</li>
     *     <li>ARGV[4]: Value to set for the lock key.</li>
     * </ul>
     * </p>
     */
    public static String LUA_LOCK_SCRIPT =
            "local set = redis.call('SETNX', KEYS[1], ARGV[4])\n" + // Set the lock key
                    "if set == 0 then return {err = 'Seat already locked'} end\n" +
                    "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +  // Set the lock key ttl
                    "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2])\n" + // Set the seat into hash
                    "redis.call('PUBLISH', KEYS[4], ARGV[3])\n" +   // Publish lock event
                    "return 'OK'";

    /**
     * Lua script for unlocking a seat in Redis.
     * <p>
     * This script performs the following operations:
     * 1. Deletes the lock key using `DEL` to release the lock.
     * 2. Removes the seat information from the Redis hash using `HDEL`.
     * 3. Publishes an unlock event to a Redis channel using `PUBLISH`.
     * </p>
     * <p>
     * Keys:
     * <ul>
     *     <li>KEYS[1]: Lock key for the seat.</li>
     *     <li>KEYS[2]: Hash key for seat reservations.</li>
     *     <li>KEYS[3]: Field in the hash representing the seat.</li>
     *     <li>KEYS[4]: Redis channel name for publishing unlock events.</li>
     * </ul>
     * Arguments:
     * <ul>
     *     <li>ARGV[1]: Message to publish to the Redis channel.</li>
     * </ul>
     * </p>
     */
    public static String LUA_UNLOCK_SCRIPT =
            "redis.call('DEL', KEYS[1])\n" +                      // Delete the lock key
                    "redis.call('HDEL', KEYS[2], KEYS[3])\n" +           // Remove the seat from hash
                    "redis.call('PUBLISH', KEYS[4], ARGV[1])\n" +        // Publish unlock event
                    "return 'OK'";
}