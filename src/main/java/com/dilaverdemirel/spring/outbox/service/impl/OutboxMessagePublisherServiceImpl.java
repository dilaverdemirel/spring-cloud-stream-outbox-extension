package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@Slf4j
@Service
public class OutboxMessagePublisherServiceImpl implements OutboxMessagePublisherService {

    private final OutboxMessageRepository outboxMessageRepository;
    private final BinderAwareChannelResolver channelResolver;

    @Value("${dilaverdemirel.spring.outbox.failed-messages.retry-count-threshold:3}")
    protected Integer retryCountThreshold;

    @Value("${dilaverdemirel.spring.outbox.failed-messages.message-life-time-in-days:7}")
    protected Integer messageLifetimeInDays;

    public OutboxMessagePublisherServiceImpl(OutboxMessageRepository outboxMessageRepository,
                                             BinderAwareChannelResolver channelResolver) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.channelResolver = channelResolver;
    }

    @Override
    @Transactional
    public void publishById(String id) {
        log.debug("Outbox message is publishing, the id is {}", id);
        final var outboxMessageOptional = outboxMessageRepository.findById(id);
        if (outboxMessageOptional.isPresent()) {
            log.debug("Outbox message with {} found", id);
            final var outboxMessage = outboxMessageOptional.get();
            sendAndMarkAsSent(outboxMessage);
        }
    }

    @Override
    @Transactional
    public void publishAllFailedMessages() {
        log.debug("Failed Outbox messages is publishing again!");
        var page = 0;
        while (true) {
            final var pageRequest = PageRequest.of(page, QUERY_RESULT_PAGE_SIZE, Sort.Direction.ASC, "createdAt");
            final var failedMessages = outboxMessageRepository
                    .findByStatusAndRetryCountLessThanEqual(OutboxMessageStatus.FAILED, retryCountThreshold, pageRequest);
            if (failedMessages.isEmpty()) {
                break;
            }
            failedMessages.forEach(this::sendAndMarkAsSent);
            page++;
        }

        log.debug("Not sent outbox messages is publishing again!");
        page = 0;
        while (true) {
            final var pageRequest = PageRequest.of(page, QUERY_RESULT_PAGE_SIZE, Sort.Direction.ASC, "createdAt");
            final var messagesThatCouldNotBeSent = outboxMessageRepository.findMessagesThatCouldNotBeSent(
                    LocalDateTime.now().minus(QUERY_DELAY_FOR_MESSAGE_THAT_COULD_NOT_BE_SENT, ChronoUnit.SECONDS),
                    pageRequest);

            if (messagesThatCouldNotBeSent.isEmpty()) {
                break;
            }
            messagesThatCouldNotBeSent.forEach(this::sendAndMarkAsSent);
            page++;
        }
    }

    @Override
    @Transactional
    public void maintenanceToOutboxMessages() {
        log.debug("Maintenance process is starting!");
        publishAllFailedMessages();
        log.debug("Outbox messages is cleaning up! Message lifetime is {}!", messageLifetimeInDays);
        final var deletedMessageCount =
                outboxMessageRepository.deleteOldOutboxMessages(LocalDateTime.now().minus(messageLifetimeInDays, ChronoUnit.DAYS));
        log.debug("{} old outbox messages deleted!", deletedMessageCount);
    }

    private void sendAndMarkAsSent(OutboxMessage outboxMessage) {
        final var messageHeaders = new MessageHeaders(Map.of(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME, outboxMessage.getId()));
        channelResolver.resolveDestination(outboxMessage.getChannel())
                .send(MessageBuilder.createMessage(outboxMessage.getPayload(), messageHeaders));

        outboxMessage.setStatus(OutboxMessageStatus.SENT);
        outboxMessage.setSentAt(LocalDateTime.now());
        outboxMessage.setRetryCount(outboxMessage.getRetryCount() + 1);
        outboxMessageRepository.save(outboxMessage);
    }
}
