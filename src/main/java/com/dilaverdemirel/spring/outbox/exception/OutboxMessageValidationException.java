package com.dilaverdemirel.spring.outbox.exception;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
public class OutboxMessageValidationException extends RuntimeException {
    public OutboxMessageValidationException(String message) {
        super(message);
    }
}
