package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.exception.OutboxMessageNotFoundException;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.dilaverdemirel.spring.outbox.service.FailedOutboxMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dilaverdemirel.spring.outbox.util.StringUtils.isBlank;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
@Slf4j
@Service
public class FailedOutboxMessageServiceImpl implements FailedOutboxMessageService {
    private final OutboxMessageRepository outboxMessageRepository;

    public FailedOutboxMessageServiceImpl(OutboxMessageRepository outboxMessageRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
    }

    @Override
    @Transactional
    public void markAsFailedWithExceptionMessage(String id, String exceptionMessage) {
        if (isBlank(id)) {
            return;
        }

        final var outboxMessageOpt = outboxMessageRepository.findById(id);
        if (outboxMessageOpt.isPresent()) {
            final var outboxMessage = outboxMessageOpt.get();
            outboxMessage.setStatus(OutboxMessageStatus.FAILED);
            outboxMessage.setStatusMessage(exceptionMessage);
            outboxMessageRepository.save(outboxMessage);
        } else {
            throw new OutboxMessageNotFoundException(String.format("There is not outbox message for id %s", id));
        }
    }
}
