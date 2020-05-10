package com.dilaverdemirel.outbox.repository;

import com.dilaverdemirel.outbox.domain.OutboxMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Repository
public interface OutboxMessageRepository extends CrudRepository<OutboxMessage, String> {
}
