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

CREATE TABLE IF NOT EXISTS seats
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id     BIGINT      NOT NULL,
    `row_number` VARCHAR(10) NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    status       ENUM ('AVAILABLE', 'BOOKED', 'LOCKED') DEFAULT 'AVAILABLE',
    created_at   TIMESTAMP                              DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP                              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_venue FOREIGN KEY (venue_id) REFERENCES venues (id),
    UNIQUE (venue_id, `row_number`, seat_number)
);

CREATE TABLE IF NOT EXISTS events
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255)              NOT NULL,
    type       ENUM ('CONCERT', 'MOVIE') NOT NULL,
    venue_id   BIGINT                    NOT NULL,
    date_time  DATETIME                  NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues (id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,
    event_id     BIGINT NOT NULL,
    status       ENUM ('PENDING', 'CONFIRMED', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP                                  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_booking_event FOREIGN KEY (event_id) REFERENCES events (id)
);

INSERT INTO users (guid, email, name, password_hash)
VALUES (X'3d7f6c340a184646ab06e6b96e0b0a55', 'main@mail.com', 'Mainusr',
        '$2a$10$WN0mfDqugBLU1eYh6LqABug2spZKu2LH93CWjefbB54HPDfLB.0Mm');

INSERT INTO venues (name, location, capacity)
VALUES ('Grand Concert Hall', '123 Music Ave', 100),
       ('Cineplex 8', '456 Film Blvd', 80);

INSERT INTO seats (venue_id, `row_number`, seat_number, status)
VALUES (1, 'A', '1', 'AVAILABLE'),
       (1, 'A', '2', 'AVAILABLE'),
       (1, 'A', '3', 'AVAILABLE'),
       (1, 'A', '4', 'AVAILABLE'),
       (1, 'A', '5', 'AVAILABLE'),
       (1, 'B', '1', 'AVAILABLE'),
       (1, 'B', '2', 'AVAILABLE'),
       (1, 'B', '3', 'AVAILABLE'),
       (1, 'B', '4', 'AVAILABLE'),
       (1, 'B', '5', 'AVAILABLE');

INSERT INTO seats (venue_id, `row_number`, seat_number, status)
VALUES (2, 'A', '1', 'AVAILABLE'),
       (2, 'A', '2', 'AVAILABLE'),
       (2, 'A', '3', 'AVAILABLE'),
       (2, 'A', '4', 'AVAILABLE'),
       (2, 'A', '5', 'AVAILABLE'),
       (2, 'B', '1', 'AVAILABLE'),
       (2, 'B', '2', 'AVAILABLE'),
       (2, 'B', '3', 'AVAILABLE'),
       (2, 'B', '4', 'AVAILABLE'),
       (2, 'B', '5', 'AVAILABLE');

INSERT INTO events (name, type, venue_id, date_time)
VALUES ('Rock Legends Live', 'CONCERT', 1, '2025-08-01 20:00:00'),
       ('Blockbuster Premiere', 'MOVIE', 2, '2025-08-02 18:30:00');
