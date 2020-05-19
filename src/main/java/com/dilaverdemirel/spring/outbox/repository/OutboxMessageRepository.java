package com.dilaverdemirel.spring.outbox.repository;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Repository
public interface OutboxMessageRepository extends CrudRepository<OutboxMessage, String> {
    Page<OutboxMessage> findByStatus(OutboxMessageStatus status, Pageable pageRequest);
}
