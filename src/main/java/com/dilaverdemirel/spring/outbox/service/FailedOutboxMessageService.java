package com.dilaverdemirel.spring.outbox.service;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
public interface FailedOutboxMessageService {
    void markAsFailedWithExceptionMessage(String id, String exceptionMessage);
}
