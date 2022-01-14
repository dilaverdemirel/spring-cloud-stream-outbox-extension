package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.dilaverdemirel.spring.outbox.service.OutboxMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author dilaverdemirel
 * @since 13/01/2022
 */
@Service
@RequiredArgsConstructor
public class OutboxMessageServiceImpl implements OutboxMessageService {

    private final OutboxMessageRepository outboxMessageRepository;

    @Override
    public Optional<OutboxMessage> getById(String id) {
        return outboxMessageRepository.findById(id);
    }

    @Override
    @Transactional
    public long deleteOldOutboxMessages(LocalDateTime thresholdDate) {
        return outboxMessageRepository.deleteOldOutboxMessages(thresholdDate);
    }
}
