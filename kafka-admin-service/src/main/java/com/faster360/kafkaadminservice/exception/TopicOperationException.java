package com.faster360.kafkaadminservice.exception;

import org.springframework.http.HttpStatus;

public class TopicOperationException extends RuntimeException {

    private final HttpStatus status;

    public TopicOperationException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
