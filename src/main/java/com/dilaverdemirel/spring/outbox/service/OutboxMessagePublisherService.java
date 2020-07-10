package com.dilaverdemirel.spring.outbox.service;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
public interface OutboxMessagePublisherService {
    int QUERY_RESULT_PAGE_SIZE = 20;
    int QUERY_DELAY_FOR_MESSAGE_THAT_COULD_NOT_BE_SENT = 30;
    String OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME = "X-Outbox-Message-Id";
    String OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME = "x-exception-stacktrace";

    void publishById(String id);

    void publishAllFailedMessages();
}
