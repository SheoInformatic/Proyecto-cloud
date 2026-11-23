package com.faster360.notificationticketservice.service;

/** Payload inválido: no tiene sentido reintentar, el mensaje se deriva directamente al DLT. */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
