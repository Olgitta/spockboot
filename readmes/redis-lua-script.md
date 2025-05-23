Here’s how to implement a **Redis Lua script** that **atomically**:

* `SETNX` the lock key with expiration (to avoid duplicate locks)
* `HSET` the timestamp for the seat in a reservation hash
* `PUBLISH` to a channel

---

## ✅ 1. **Lua Script Logic**

```lua
-- Arguments: KEYS[1] = lockKey
--            ARGV[1] = expiration in seconds
--            KEYS[2] = hashKey
--            KEYS[3] = field
--            ARGV[2] = timestamp
--            KEYS[4] = channel
--            ARGV[3] = message

-- Try to set the lock with expiration
local set = redis.call("SETNX", KEYS[1], "1")
if set == 0 then
    return {err = "Seat already locked"}
end
redis.call("EXPIRE", KEYS[1], tonumber(ARGV[1]))

-- Write to the hash
redis.call("HSET", KEYS[2], KEYS[3], ARGV[2])

-- Publish event
redis.call("PUBLISH", KEYS[4], ARGV[3])

return "OK"
```

---

## ✅ 2. **Spring Boot Java Implementation**

Add a Lua script loader in your `RedisService`:

```java
public String lockSeatAtomic(
        String lockKey, int expireSeconds,
        String hashKey, String field, String timestamp,
        String channel, String message) {

    DefaultRedisScript<String> script = new DefaultRedisScript<>();
    script.setScriptText(LUA_LOCK_SCRIPT);
    script.setResultType(String.class);

    List<String> keys = Arrays.asList(lockKey, hashKey, field, channel);
    List<String> args = Arrays.asList(
        String.valueOf(expireSeconds),
        timestamp,
        message
    );

    return stringRedisTemplate.execute(script, keys, args.toArray());
}
```

Define `LUA_LOCK_SCRIPT` (could be loaded from file or hardcoded):

```java
private static final String LUA_LOCK_SCRIPT = 
    "local set = redis.call('SETNX', KEYS[1], '1')\n" +
    "if set == 0 then return {err = 'Seat already locked'} end\n" +
    "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))\n" +
    "redis.call('HSET', KEYS[2], KEYS[3], ARGV[2])\n" +
    "redis.call('PUBLISH', KEYS[4], ARGV[3])\n" +
    "return 'OK'";
```

---

## ✅ 3. Usage in `SeatsService`

```java
public void lock(Long eventId, Long venueId, String rowNumber, String seatNumber) {
    String hashKey = RedisKeyFactory.reservationKey(eventId);
    String channelName = RedisKeyFactory.channel(eventId);
    String field = RedisKeyFactory.reservationField(venueId, rowNumber, seatNumber);
    String lockKey = RedisKeyFactory.lockKey(eventId, venueId, rowNumber, seatNumber);
    String timestamp = Instant.now().toString();

    String result = redisService.lockSeatAtomic(
        lockKey, 600, // 10 min
        hashKey, field, timestamp,
        channelName, lockKey
    );

    if (!"OK".equals(result)) {
        throw new SeatAlreadyLockedException(lockKey);
    }
}
```

---

## ✅ Benefits

* **All-or-nothing** behavior
* No partial states
* Eliminates need for manual rollback

Let me know if you'd like this logic refactored into utility or external Lua script files.
