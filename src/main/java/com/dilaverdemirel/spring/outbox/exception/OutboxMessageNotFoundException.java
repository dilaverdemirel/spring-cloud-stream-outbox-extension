package com.dilaverdemirel.spring.outbox.exception;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
public class OutboxMessageNotFoundException extends RuntimeException {
    public OutboxMessageNotFoundException(String message) {
    }
}
