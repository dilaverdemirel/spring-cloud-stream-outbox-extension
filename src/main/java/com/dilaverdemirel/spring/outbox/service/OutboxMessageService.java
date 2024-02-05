package com.dilaverdemirel.spring.outbox.service;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author dilaverdemirel
 * @since 13/01/2022
 */
@Validated
public interface OutboxMessageService {
    Optional<OutboxMessage> getById(@NotEmpty String id);

    long deleteOldOutboxMessages(@NotNull LocalDateTime thresholdDate);
}
