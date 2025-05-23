CREATE DATABASE IF NOT EXISTS spockboot;
USE spockboot;

CREATE TABLE IF NOT EXISTS users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    guid          BINARY(16)   NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS venues
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    capacity   INT          NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seat_statuses
(
    id   TINYINT PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS seats
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id     BIGINT      NOT NULL,
    `row_number` VARCHAR(10) NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS event_types
(
    id   TINYINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    event_type_id TINYINT      NOT NULL,
    venue_id      BIGINT       NOT NULL,
    date_time     DATETIME     NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_type FOREIGN KEY (event_type_id) REFERENCES event_types (id)
);

CREATE TABLE IF NOT EXISTS seats_events
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id     BIGINT      NOT NULL,
    venue_id     BIGINT      NOT NULL,
    `row_number` VARCHAR(10) NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    status_id    TINYINT     NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE,
    CONSTRAINT fk_seats_events_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_seats_events_status FOREIGN KEY (status_id) REFERENCES seat_statuses (id),
    UNIQUE (event_id, venue_id, `row_number`, seat_number)
);

CREATE TABLE IF NOT EXISTS order_statuses
(
    id   TINYINT PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    event_id     BIGINT  NOT NULL,
    status_id    TINYINT NOT NULL,
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status FOREIGN KEY (status_id) REFERENCES order_statuses (id)
);

-- Seed status types
INSERT INTO seat_statuses (id, name)
VALUES (1, 'AVAILABLE'),
       (2, 'LOCKED'),
       (3, 'BOOKED');

-- Seed event types
INSERT INTO event_types (id, name)
VALUES (1, 'CONCERT'),
       (2, 'MOVIE');

-- Seed order statuses
INSERT INTO order_statuses (id, name)
VALUES (1, 'PENDING'),
       (2, 'CONFIRMED'),
       (3, 'CANCELLED');

-- Add one user
INSERT INTO users (guid, email, name, password_hash)
VALUES (X'3d7f6c340a184646ab06e6b96e0b0a55', 'admin@mail.com', 'Admin',
        '$2a$10$WN0mfDqugBLU1eYh6LqABug2spZKu2LH93CWjefbB54HPDfLB.0Mm');

-- Add venues
INSERT INTO venues (name, location, capacity)
VALUES ('Grand Concert Hall', '123 Music Ave', 100),
       ('Cineplex 8', '456 Film Blvd', 80);

-- Add seats (venue 1, status_id = 1)
INSERT INTO seats (venue_id, `row_number`, seat_number)
VALUES (1, 'A', '1'),
       (1, 'A', '2'),
       (1, 'A', '3'),
       (1, 'A', '4'),
       (1, 'A', '5'),
       (1, 'B', '1'),
       (1, 'B', '2'),
       (1, 'B', '3'),
       (1, 'B', '4'),
       (1, 'B', '5');

-- Add seats (venue 2, status_id = 1)
INSERT INTO seats (venue_id, `row_number`, seat_number)
VALUES (2, 'A', '1'),
       (2, 'A', '2'),
       (2, 'A', '3'),
       (2, 'A', '4'),
       (2, 'A', '5'),
       (2, 'B', '1'),
       (2, 'B', '2'),
       (2, 'B', '3'),
       (2, 'B', '4'),
       (2, 'B', '5');

-- Add events (event_type_id from event_types, venue_id from venues)
INSERT INTO events (name, event_type_id, venue_id, date_time)
VALUES ('Rock Legends Live', 1, 1, '2025-08-01 20:00:00'),
       ('Blockbuster Premiere', 2, 2, '2025-08-02 18:30:00');

-- Insert into seats_events for event_id = 1, venue_id = 1, status_id = 1
INSERT INTO seats_events (event_id, venue_id, `row_number`, seat_number, status_id)
VALUES (1, 1, 'A', '1', 1),
       (1, 1, 'A', '2', 1),
       (1, 1, 'A', '3', 1),
       (1, 1, 'A', '4', 1),
       (1, 1, 'A', '5', 1),
       (1, 1, 'B', '1', 1),
       (1, 1, 'B', '2', 1),
       (1, 1, 'B', '3', 1),
       (1, 1, 'B', '4', 1),
       (1, 1, 'B', '5', 1);
