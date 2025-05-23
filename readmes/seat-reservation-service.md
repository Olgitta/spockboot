Great — here's a **clean and scalable design** for your **Seat Lock Service** (`seat-lock-service`), covering:

1. REST API endpoints (for synchronous requests)
2. Kafka topics & message schemas (for async/event-driven flow)
3. Internal Redis interactions
4. Unlocking flow with Redis key expiration events (as discussed)

---

## 🧩 1. REST API Design

### 🔒 Lock a seat

```http
POST /locks
```

**Request Body:**

```json
{
  "venueId": "1",
  "row": "A",
  "seat": "12",
  "userId": "u123",
  "timeoutSeconds": 120
}
```

**Response:**

```json
{
  "status": "LOCKED",
  "expiresAt": "2025-05-20T12:01:15Z"
}
```

---

### 🔓 Unlock a seat manually

```http
DELETE /locks
```

**Request Body:**

```json
{
  "venueId": "1",
  "row": "A",
  "seat": "12"
}
```

**Response:**

```json
{
  "status": "UNLOCKED"
}
```

---

### 📊 Check seat status

```http
GET /seats/status?venueId=1&row=A&seat=12
```

**Response:**

```json
{
  "seat": "A-12",
  "status": "LOCKED",
  "lockedBy": "u123",
  "expiresAt": "2025-05-20T12:01:15Z"
}
```

---

## 🔁 2. Kafka Topics & Messages

### 🔒 `seat.lock.requested` (sent by frontend or booking service)

```json
{
  "venueId": "1",
  "row": "A",
  "seat": "12",
  "userId": "u123",
  "timeoutSeconds": 120
}
```

### 📣 `seat.locked` (emitted by seat-lock-service)

```json
{
  "venueId": "1",
  "row": "A",
  "seat": "12",
  "userId": "u123",
  "lockedAt": "2025-05-20T12:00:00Z",
  "expiresAt": "2025-05-20T12:02:00Z"
}
```

### 🔓 `seat.unlocked` (emitted on timeout or manual unlock)

```json
{
  "venueId": "1",
  "row": "A",
  "seat": "12",
  "reason": "expired", // or "manual"
  "unlockedAt": "2025-05-20T12:02:00Z"
}
```

These events let other services (like booking, UI updater, etc.) respond to seat availability in real-time.

---

## 🗃️ 3. Redis Data Model

Use **hashes per venue**:

```
Key: seats:<venueId>
Fields:
  A_12 = {
    "status": "LOCKED",
    "userId": "u123",
    "expiresAt": "2025-05-20T12:02:00Z"
  }
```

Use expiring lock keys:

```
SET seatlock:<venueId>:<row>:<seat> "1" EX 120
```

---

## 🔔 4. Real-time Unlock via Expiration Listener

Redis key expires: `seatlock:1:A:12`

→ Redis pub/sub event `__keyevent@0__:expired`

→ Your Spring listener detects it and:

1. Updates the hash: set seat status to `"AVAILABLE"`
2. Publishes Kafka event: `seat.unlocked` with reason `"expired"`

Already covered earlier in [this answer](https://chat.openai.com/share/13fd3d7e-1b1e-4b7e-94be-2d190d4076e4).

---

## 🧠 Bonus: Booking Service Integration

* When booking is finalized:

    * Booking service sends `seat.finalize` event
    * Seat-lock-service permanently removes the lock
    * Emits a `seat.finalized` event (optional)

---

Would you like help implementing Kafka producers/consumers with Spring for this flow?
