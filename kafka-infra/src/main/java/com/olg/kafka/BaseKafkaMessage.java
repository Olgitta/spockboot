package com.olg.kafka;

public class BaseKafkaMessage<T> {
    private String eventType;
    private T payload;

    public BaseKafkaMessage() {}

    public BaseKafkaMessage(String eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}

