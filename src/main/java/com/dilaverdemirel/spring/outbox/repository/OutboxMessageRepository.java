package com.dilaverdemirel.spring.outbox.repository;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Repository
public interface OutboxMessageRepository extends CrudRepository<OutboxMessage, String> {
    Page<OutboxMessage> findByStatusAndRetryCountLessThanEqual(OutboxMessageStatus status, Integer retryCount, Pageable pageRequest);

    @Query("select m from OutboxMessage as m where m.createdAt <= :delayStart and m.status = 'NEW'")
    Page<OutboxMessage> findMessagesThatCouldNotBeSent(@Param("delayStart") LocalDateTime delayStart, Pageable pageRequest);

    @Modifying
    @Query("delete from OutboxMessage as m where m.createdAt >= :thresholdDate")
    long deleteOldOutboxMessages(@Param("thresholdDate") LocalDateTime thresholdDate);
}
