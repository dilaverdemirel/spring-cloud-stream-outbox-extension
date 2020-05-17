package com.dilaverdemirel.outbox.listener;

import com.dilaverdemirel.outbox.domain.OutboxMessage;
import com.dilaverdemirel.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.outbox.dto.OutboxMessageEvent;
import com.dilaverdemirel.outbox.dto.OutboxMessageEventMetaData;
import com.dilaverdemirel.outbox.exception.OutboxMessageValidationException;
import com.dilaverdemirel.outbox.repository.OutboxMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static com.dilaverdemirel.outbox.util.JsonUtil.convertToJson;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Component
public class OutboxMessageHandler {
    private final OutboxMessageRepository outboxMessageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OutboxMessageHandler(OutboxMessageRepository outboxMessageRepository,
                                ApplicationEventPublisher applicationEventPublisher) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener(OutboxMessageEvent.class)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboxMessageCreate(OutboxMessageEvent outboxMessageEvent) {
        validateOutboxMessage(outboxMessageEvent);
        final var savedOutboxMessage = saveMessage(outboxMessageEvent);
        applicationEventPublisher.publishEvent(OutboxMessageEventMetaData.builder().messageId(savedOutboxMessage.getId()).build());
    }

    private OutboxMessage saveMessage(OutboxMessageEvent outboxMessageEvent) {
        final var outboxMessage = OutboxMessage.builder()
                .id(UUID.randomUUID().toString())
                .source(outboxMessageEvent.getSource())
                .sourceId(outboxMessageEvent.getSourceId())
                .channel(outboxMessageEvent.getChannel())
                .payload(convertToJson(outboxMessageEvent.getPayload()))
                .createdAt(new Date())
                .messageClass(outboxMessageEvent.getPayload().getClass().getName())
                .status(OutboxMessageStatus.NEW).build();

        return outboxMessageRepository.save(outboxMessage);
    }

    private void validateOutboxMessage(OutboxMessageEvent outboxMessageEvent) {
        if (isBlank(outboxMessageEvent.getSource()) ||
                isBlank(outboxMessageEvent.getSourceId()) ||
                isBlank(outboxMessageEvent.getChannel()) ||
                Objects.isNull(outboxMessageEvent.getPayload())
        ) {
            throw new OutboxMessageValidationException("Please enter all fields data for outbox message send!");
        }
    }

    private boolean isBlank(String data) {
        return Objects.isNull(data) ||
                data.equals("") ||
                data.equals(" ");
    }
}
