package com.dilaverdemirel.outbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DummyMessagePayload {
    private String id;
    private String name;
}
