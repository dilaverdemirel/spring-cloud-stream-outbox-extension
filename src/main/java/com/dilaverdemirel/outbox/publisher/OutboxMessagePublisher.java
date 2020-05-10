package com.dilaverdemirel.outbox.publisher;

import com.dilaverdemirel.outbox.domain.OutboxMessage;
import com.dilaverdemirel.outbox.dto.OutboxMessageEvent;
import com.dilaverdemirel.outbox.exception.OutboxMessagePayloadJsonConvertException;
import com.dilaverdemirel.outbox.exception.OutboxMessageValidationException;
import com.dilaverdemirel.outbox.repository.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;
import java.util.UUID;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
public class OutboxMessagePublisher {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final BinderAwareChannelResolver channelResolver;
    private final OutboxMessageRepository outboxMessageRepository;

    public OutboxMessagePublisher(BinderAwareChannelResolver binderAwareChannelResolver,
                                  OutboxMessageRepository outboxMessageRepository) {
        this.channelResolver = binderAwareChannelResolver;
        this.outboxMessageRepository = outboxMessageRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener(OutboxMessageEvent.class)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxMessageSent(OutboxMessageEvent outboxMessageEvent) {
        validateOutboxMessage(outboxMessageEvent);
        final var payloadJson = convertPayloadToJson(outboxMessageEvent);
        try {
            channelResolver.resolveDestination(outboxMessageEvent.getChannel())
                    .send(MessageBuilder.withPayload(payloadJson).build());
        } catch (Throwable enyException) {
            saveMessage(outboxMessageEvent, payloadJson);
        }
    }

    private String convertPayloadToJson(OutboxMessageEvent outboxMessageEvent) {
        try {
            return OBJECT_MAPPER.writeValueAsString(outboxMessageEvent.getPayload());
        } catch (JsonProcessingException processingException) {
            throw new OutboxMessagePayloadJsonConvertException("Payload is not eligible to json conversion!");
        }
    }

    private void saveMessage(OutboxMessageEvent outboxMessageEvent, String payloadJson) {
        final var outboxMessage = OutboxMessage.builder()
                .id(UUID.randomUUID().toString())
                .source(outboxMessageEvent.getSource())
                .sourceId(outboxMessageEvent.getSourceId())
                .channel(outboxMessageEvent.getChannel())
                .payload(payloadJson)
                .status(OutboxMessage.Status.NEW).build();

        outboxMessageRepository.save(outboxMessage);
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
