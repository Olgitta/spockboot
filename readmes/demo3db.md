Here's a starting point for the **database schema** creation scripts for your Event Booking System. We'll use **MySQL** as the database engine, but it's easy to adapt for PostgreSQL if needed.

---

### ✅ `schema.sql`

```sql
-- Events table
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type ENUM('CONCERT', 'MOVIE') NOT NULL,
    date_time DATETIME NOT NULL,
    venue VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seats table
CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    row_number VARCHAR(10) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'LOCKED') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Users table (optional if not using auth)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bookings table
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    event_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_booking_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Booking_Seats join table (many-to-many)
CREATE TABLE booking_seats (
    booking_id BIGINT,
    seat_id BIGINT,
    PRIMARY KEY (booking_id, seat_id),
    CONSTRAINT fk_bs_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_bs_seat FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE
);
```

---

### 🔧 Optional Enhancements

* Add `INDEX` on `event_id`, `seat.status`, and `booking.status` for performance.
* You can also normalize seat layout if venues are reused across events.

---

Would you like me to also provide:

* The matching **JPA entity classes**?
* Docker Compose setup for MySQL + Kafka?
* Initial Spring Boot project structure?
