package com.dilaverdemirel.spring.outbox.exception;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
public class OutboxMessagePayloadJsonConvertException extends RuntimeException {
    public OutboxMessagePayloadJsonConvertException(String message) {
        super(message);
    }
}
