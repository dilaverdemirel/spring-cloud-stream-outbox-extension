package com.dilaverdemirel.outbox.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Builder
@Getter
public class OutboxMessageEvent {
    private final String source;
    private final String sourceId;
    private final Object payload;
    private final String channel;
}
