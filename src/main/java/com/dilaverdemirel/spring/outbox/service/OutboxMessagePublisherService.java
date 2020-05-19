package com.dilaverdemirel.spring.outbox.service;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
public interface OutboxMessagePublisherService {
    int QUERY_RESULT_PAGE_SIZE = 20;

    void publishById(String id);

    void publishAllFailedMessages();
}
