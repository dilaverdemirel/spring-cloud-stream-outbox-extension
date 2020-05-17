package com.dilaverdemirel.outbox.listener;

import com.dilaverdemirel.outbox.dto.OutboxMessageEventMetaData;
import com.dilaverdemirel.outbox.service.OutboxMessagePublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@Slf4j
@Component
public class OutboxMessagePublisher {

    private final OutboxMessagePublisherService outboxMessagePublisherService;

    public OutboxMessagePublisher(OutboxMessagePublisherService outboxMessagePublisherService) {
        this.outboxMessagePublisherService = outboxMessagePublisherService;
    }

    @EventListener(OutboxMessageEventMetaData.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOutboxMessageSave(OutboxMessageEventMetaData outboxMessageEventMetaData) {
        log.debug("Outbox message is publishing, meta data is {}", outboxMessageEventMetaData);
        outboxMessagePublisherService.publishById(outboxMessageEventMetaData.getMessageId());
    }
}
