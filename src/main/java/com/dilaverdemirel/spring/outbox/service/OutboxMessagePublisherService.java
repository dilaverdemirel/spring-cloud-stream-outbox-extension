package com.dilaverdemirel.spring.outbox.service;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
public interface OutboxMessagePublisherService {
    int QUERY_RESULT_PAGE_SIZE = 20;
    int QUERY_DELAY_FOR_MESSAGE_THAT_COULD_NOT_BE_SENT = 30;

    void publishById(String id);

    void publishAllFailedMessages();
}
