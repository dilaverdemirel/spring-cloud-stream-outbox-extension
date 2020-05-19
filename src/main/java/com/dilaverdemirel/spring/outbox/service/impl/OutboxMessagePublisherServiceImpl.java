package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@Slf4j
@Service
public class OutboxMessagePublisherServiceImpl implements OutboxMessagePublisherService {

    private final OutboxMessageRepository outboxMessageRepository;

    private final BinderAwareChannelResolver channelResolver;

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
        final var pageRequest = PageRequest.of(0, QUERY_RESULT_PAGE_SIZE, Sort.Direction.ASC, "createdAt");
        final var failedOutboxMessages = outboxMessageRepository.findByStatus(OutboxMessageStatus.FAILED, pageRequest);
        failedOutboxMessages.forEach(message -> sendAndMarkAsSent(message));
    }

    private void sendAndMarkAsSent(OutboxMessage outboxMessage) {
        channelResolver.resolveDestination(outboxMessage.getChannel())
                .send(MessageBuilder.withPayload(outboxMessage.getPayload()).build());

        outboxMessage.setStatus(OutboxMessageStatus.SENT);
        outboxMessage.setSentAt(new Date());
        outboxMessageRepository.save(outboxMessage);
    }
}
