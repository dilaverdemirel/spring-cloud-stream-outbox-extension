package com.dilaverdemirel.spring.outbox.scheduler;


import com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
public class FailedOutboxMessageSchedulerService {
    @Autowired
    private OutboxMessagePublisherService outboxMessagePublisherService;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void sendFailedMessages() {
        outboxMessagePublisherService.publishAllFailedMessages();
    }
}
