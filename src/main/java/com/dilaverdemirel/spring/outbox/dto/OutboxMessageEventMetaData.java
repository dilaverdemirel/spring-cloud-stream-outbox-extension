package com.dilaverdemirel.spring.outbox.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@Builder
@Getter
@ToString
public class OutboxMessageEventMetaData {
    private final String messageId;
}
