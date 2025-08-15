-- PostgreSQL не использует 'CREATE DATABASE IF NOT EXISTS' в скриптах,
-- так как подключение уже происходит к конкретной базе данных.
-- Также не используется команда 'USE'.

-- Создаем таблицы
-- Заменяем BIGINT AUTO_INCREMENT на BIGSERIAL
-- Заменяем BINARY(16) на UUID и используем тип UUID
-- Заменяем TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP на триггер
-- Заменяем TINYINT на SMALLINT
-- Заменяем обратные кавычки (`) на двойные кавычки (") для `row_number`

CREATE TABLE IF NOT EXISTS users
(
    id            BIGSERIAL PRIMARY KEY,
    guid          UUID         NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS venues
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    location   VARCHAR(255) NOT NULL,
    capacity   INTEGER      NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seat_statuses
(
    id   SMALLINT PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS seats
(
    id           BIGSERIAL PRIMARY KEY,
    venue_id     BIGINT      NOT NULL,
    "row_number" VARCHAR(10) NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS event_types
(
    id   SMALLINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    event_type_id SMALLINT     NOT NULL,
    venue_id      BIGINT       NOT NULL,
    date_time     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_type FOREIGN KEY (event_type_id) REFERENCES event_types (id)
);

CREATE TABLE IF NOT EXISTS seats_events
(
    id           BIGSERIAL PRIMARY KEY,
    event_id     BIGINT      NOT NULL,
    venue_id     BIGINT      NOT NULL,
    "row_number" VARCHAR(10) NOT NULL,
    seat_number  VARCHAR(10) NOT NULL,
    status_id    SMALLINT    NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_seats_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id) ON DELETE CASCADE,
    CONSTRAINT fk_seats_events_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_seats_events_status FOREIGN KEY (status_id) REFERENCES seat_statuses (id),
    UNIQUE (event_id, venue_id, "row_number", seat_number)
);

CREATE TABLE IF NOT EXISTS order_statuses
(
    id   SMALLINT PRIMARY KEY,
    name VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS orders
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT,
    event_id     BIGINT  NOT NULL,
    status_id    SMALLINT NOT NULL,
    total_amount DECIMAL(10, 2),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_status FOREIGN KEY (status_id) REFERENCES order_statuses (id)
);

-- Создаем функцию для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Применяем триггер к каждой таблице, где есть updated_at
CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_venues_updated_at
BEFORE UPDATE ON venues
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_seats_updated_at
BEFORE UPDATE ON seats
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_events_updated_at
BEFORE UPDATE ON events
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_seats_events_updated_at
BEFORE UPDATE ON seats_events
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


-- Заполняем таблицы данными
INSERT INTO seat_statuses (id, name)
VALUES (1, 'AVAILABLE'),
       (2, 'LOCKED'),
       (3, 'BOOKED');

INSERT INTO event_types (id, name)
VALUES (1, 'CONCERT'),
       (2, 'MOVIE');

INSERT INTO order_statuses (id, name)
VALUES (1, 'PENDING'),
       (2, 'CONFIRMED'),
       (3, 'CANCELLED');

-- Для UUID в PostgreSQL используем строковый формат, а не бинарный
INSERT INTO users (guid, email, name, password_hash)
VALUES ('3d7f6c34-0a18-4646-ab06-e6b96e0b0a55', 'admin@mail.com', 'Admin',
        '$2a$10$WN0mfDqugBLU1eYh6LqABug2spZKu2LH93CWjefbB54HPDfLB.0Mm');

INSERT INTO venues (name, location, capacity)
VALUES ('Grand Concert Hall', '123 Music Ave', 100),
       ('Cineplex 8', '456 Film Blvd', 80);

INSERT INTO seats (venue_id, "row_number", seat_number)
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

INSERT INTO seats (venue_id, "row_number", seat_number)
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

INSERT INTO events (name, event_type_id, venue_id, date_time)
VALUES ('Rock Legends Live', 1, 1, '2025-08-01 20:00:00'),
       ('Blockbuster Premiere', 2, 2, '2025-08-02 18:30:00');

INSERT INTO seats_events (event_id, venue_id, "row_number", seat_number, status_id)
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
