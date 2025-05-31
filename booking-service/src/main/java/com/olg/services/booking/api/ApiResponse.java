package com.olg.services.booking.api;

public class ApiResponse<T> {

    public record Metadata(String requestId) {}

    private Metadata metadata;
    private T data;

    public ApiResponse(String requestId, T data) {
        this.metadata = new Metadata(requestId);
        this.data = data;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public T getData() {
        return data;
    }
}

