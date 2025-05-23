Yes — we can **significantly improve the structure** of how you store seat availability in Redis by using **Redis hashes** or **bitmaps**, depending on your performance and scalability needs.

Let’s look at good, better, and best practices:

---

## ✅ Current structure (what you're using):

```text
Key:    seats_1_1_1
Value:  "AVAILABLE" / "LOCKED"
```

### ❌ Problems:

* Many keys (`N` keys for `N` seats) → inefficient
* Pattern search (`KEYS seats_1*`) is expensive
* Hard to manage group operations (e.g., lock a whole row or venue)

---

## ✅ Recommended Option: Use Redis Hashes

### 🔄 Store all seats for a venue as a **hash**

Key: `seats:<venue_id>`
Field: `<row>_<seat>`
Value: `"AVAILABLE"` or `"LOCKED"`

#### Example:

```text
Key: seats:1
Fields:
  "1_1" => "AVAILABLE"
  "1_2" => "LOCKED"
  "2_1" => "AVAILABLE"
```

#### In Redis CLI:

```bash
HSET seats:1 1_1 AVAILABLE
HSET seats:1 1_2 LOCKED
HGET seats:1 1_2
HGETALL seats:1
```

#### ✅ Benefits:

* Single key per venue → efficient storage
* Easy to read all seats in one HGETALL
* Efficient atomic updates (HSET, HDEL)
* Clean structure, works well for rows/seats

#### ✅ Java code using `StringRedisTemplate`:

```java
public void setSeatStatus(String venueId, String row, String seat, String status) {
    String hashKey = "seats:" + venueId;
    String field = row + "_" + seat;
    stringRedisTemplate.opsForHash().put(hashKey, field, status);
}

public String getSeatStatus(String venueId, String row, String seat) {
    String hashKey = "seats:" + venueId;
    String field = row + "_" + seat;
    Object result = stringRedisTemplate.opsForHash().get(hashKey, field);
    return result != null ? result.toString() : null;
}

public Map<Object, Object> getAllSeats(String venueId) {
    return stringRedisTemplate.opsForHash().entries("seats:" + venueId);
}
```

---

## 🚀 Advanced Option: Use Redis Bitmaps (for large-scale seating)

If performance, memory, or atomicity is critical (e.g., stadium with 10K+ seats), consider:

* Key: `seats:<venue_id>:<row>`
* Store each seat as 1 bit (`0` = AVAILABLE, `1` = LOCKED)
* Use `SETBIT`, `GETBIT` to manipulate seats

#### ✅ Pros:

* Blazing fast
* Compact storage (each seat = 1 bit)
* Atomic operations for locking seats

#### ❌ Cons:

* Harder to debug / no human-readable values
* You have to map seat numbers to bit offsets manually

> Let me know if you're interested in a bitmap implementation too.

---

## 🧠 Recommendation

Use **hashes** unless:

* You need extreme performance & compactness
* You handle very large venues
* You already map seating to linear indexes

---

Great choice using Redis hashes — it's efficient and scalable. Now, for unlocking *locked* seats after a timeout (e.g. user abandoned booking), you have a few solid options.

---

## ✅ Recommended Strategy: Use Redis `SET` with Expiry as a Lock Marker

Since Redis **hash fields don't support TTLs**, you need a **secondary structure** to track expirations.

### 👇 Solution Outline:

1. Keep seat status in the hash:
   `HSET seats:<venue_id> <row>_<seat> LOCKED`

2. Create a **temporary key** (lock marker) with expiry:
   `SET seatlock:<venue_id>:<row>:<seat> 1 EX 300` (5 min)

3. When the temporary key expires, you unlock the seat:

    * Run a background task (e.g., Redis keyspace event listener or poller)
    * Or use Redis Streams / pub-sub if you want event-based processing

---

## 🧩 Example Implementation

### 1. Lock the seat (with timeout)

```java
public void lockSeat(String venueId, String row, String seat, int timeoutSeconds) {
    String hashKey = "seats:" + venueId;
    String field = row + "_" + seat;
    String lockKey = "seatlock:" + venueId + ":" + row + ":" + seat;

    // Set seat status to LOCKED
    stringRedisTemplate.opsForHash().put(hashKey, field, "LOCKED");

    // Set expiring lock marker
    stringRedisTemplate.opsForValue().set(lockKey, "1", Duration.ofSeconds(timeoutSeconds));
}
```

### 2. Periodically scan for expired locks and unlock seats (polling)

This requires your app to **check for missing lock keys** and unlock the seat in the hash:

```java
public void unlockExpiredSeats(String venueId) {
    Map<Object, Object> seats = stringRedisTemplate.opsForHash().entries("seats:" + venueId);

    for (Map.Entry<Object, Object> entry : seats.entrySet()) {
        String field = entry.getKey().toString(); // e.g. "1_1"
        String status = entry.getValue().toString();

        if ("LOCKED".equals(status)) {
            String lockKey = "seatlock:" + venueId + ":" + field.replace("_", ":");

            Boolean exists = stringRedisTemplate.hasKey(lockKey);
            if (Boolean.FALSE.equals(exists)) {
                // Lock expired → unlock
                stringRedisTemplate.opsForHash().put("seats:" + venueId, field, "AVAILABLE");
            }
        }
    }
}
```

You can run this every 30 seconds or minute via a `@Scheduled` task or an external worker.

---

## ⚡ Advanced Option: Redis Key Expiration Events

You can configure Redis to **emit key expiration events**, so your service listens and unlocks seats *immediately* when `seatlock:*` keys expire.

### Setup in `redis.conf` or at runtime:

```bash
CONFIG SET notify-keyspace-events Ex
```

### Listen using Spring Redis:

```java
@Configuration
public class RedisConfig {
    @Bean
    public MessageListenerAdapter listenerAdapter(ExpiredKeyListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }
}
```

### Handle expired lock key:

```java
@Component
public class ExpiredKeyListener {

    @Autowired
    private RedisService redisService;

    public void handleMessage(String expiredKey) {
        if (expiredKey.startsWith("seatlock:")) {
            // Extract venue, row, seat and unlock
            String[] parts = expiredKey.split(":");
            String venueId = parts[1];
            String row = parts[2];
            String seat = parts[3];

            redisService.unlockSeat(venueId, row, seat);
        }
    }
}
```

---

## ✅ Summary

| Method                      | Pros                    | Cons                           |
| --------------------------- | ----------------------- | ------------------------------ |
| 🔁 Polling for expired keys | Simple, easy to control | Slight delay, more Redis reads |
| 📣 Redis expiration events  | Real-time, efficient    | Needs config + listener setup  |

---

Perfect — here’s a **complete working example** of real-time seat unlock using **Redis key expiration events** with Spring Boot and `StringRedisTemplate`.

---

## ✅ Prerequisites

1. Redis must be configured to publish expiration events:

    * At runtime: `CONFIG SET notify-keyspace-events Ex`
2. You lock seats using an expiring key like `seatlock:<venueId>:<row>:<seat>`
3. You listen for the key expiration event and unlock the seat in the hash.

---

## 🧩 Step-by-step Implementation

---

### 🔧 1. Enable Redis Key Expiration Notifications (Runtime)

Add a Spring `@PostConstruct` method:

```java
@Component
public class RedisEventConfigurer {

    private final StringRedisTemplate redisTemplate;

    public RedisEventConfigurer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void enableKeyspaceNotifications() {
        redisTemplate.getConnectionFactory()
            .getConnection()
            .setConfig("notify-keyspace-events", "Ex");
    }
}
```

---

### 🎧 2. Redis Key Expiry Listener

```java
@Component
public class RedisKeyExpirationListener implements MessageListener {

    @Autowired
    private RedisService redisService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString(); // e.g. seatlock:1:1:2
        if (expiredKey.startsWith("seatlock:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length == 4) {
                String venueId = parts[1];
                String row = parts[2];
                String seat = parts[3];

                redisService.unlockSeat(venueId, row, seat);
            }
        }
    }
}
```

---

### 🏗️ 3. Redis Message Listener Configuration

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener listener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Listen to keyevent events (expired keys) on DB 0
        container.addMessageListener(listener, new PatternTopic("__keyevent@0__:expired"));

        return container;
    }
}
```

---

### 🔐 4. Your `RedisService` Class with Unlock Logic

```java
@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void lockSeat(String venueId, String row, String seat, Duration timeout) {
        String hashKey = "seats:" + venueId;
        String field = row + "_" + seat;
        String lockKey = "seatlock:" + venueId + ":" + row + ":" + seat;

        redisTemplate.opsForHash().put(hashKey, field, "LOCKED");
        redisTemplate.opsForValue().set(lockKey, "1", timeout);
    }

    public void unlockSeat(String venueId, String row, String seat) {
        String hashKey = "seats:" + venueId;
        String field = row + "_" + seat;

        Object status = redisTemplate.opsForHash().get(hashKey, field);
        if ("LOCKED".equals(status)) {
            redisTemplate.opsForHash().put(hashKey, field, "AVAILABLE");
            System.out.printf("Unlocked seat: %s:%s:%s%n", venueId, row, seat);
        }
    }
}
```

---

### ✅ Example Usage

```java
// Locks seat for 2 minutes
redisService.lockSeat("1", "1", "2", Duration.ofMinutes(2));
```

---

## 🧪 Testing Tips

In your Redis CLI, you can verify:

```bash
127.0.0.1:6379> CONFIG SET notify-keyspace-events Ex
127.0.0.1:6379> KEYS *
# should show: seats:1, seatlock:1:1:2
```

After 2 minutes, key `seatlock:1:1:2` will expire, triggering your listener, and `seats:1` will update the corresponding field to `AVAILABLE`.

---

## ✅ Summary

* You use `seatlock:*` with expiry to manage temporary locks
* Redis notifies your app via Pub/Sub when the key expires
* You listen with `__keyevent@0__:expired` and clean up in real-time
* Fast, reliable, and scalable

Let me know if you want to include support for a **queue of expired events** or **retries in case of failure**.
