package com.olg.services.booking.exceptions;

public enum SeatErrorCodes {
    // Общие ошибки операций с местами
    SEAT_OPERATION_FAILED("SEAT-001", "Generic seat operation failed"),
    SEAT_LOCK_FAILED("SEAT-002", "Failed to acquire lock for seat"),
    SEAT_UNLOCK_FAILED("SEAT-003", "Failed to release lock for seat"),
    SEAT_DATA_RETRIEVAL_FAILED("SEAT-004", "Failed to retrieve seat data"),

    // Ошибки, связанные с Redis
    REDIS_DATA_ACCESS_ERROR("REDIS-001", "Error accessing Redis data"),
    REDIS_SCRIPT_EXECUTION_ERROR("REDIS-002", "Error executing Redis Lua script"),

    // Ошибки сериализации/десериализации
    JSON_PROCESSING_ERROR("JSON-001", "Error processing JSON data"),

    // Ошибки Kafka
    KAFKA_SEND_FAILED("KAFKA-001", "Failed to send message to Kafka"),

    DATABASE_ACCESS_ERROR("DB-001", "Error accessing database"),
    INVALID_KEY_FORMAT("PARSE-001", "Invalid format for Redis key or field");

    private final String code;
    private final String description;

    SeatErrorCodes(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
