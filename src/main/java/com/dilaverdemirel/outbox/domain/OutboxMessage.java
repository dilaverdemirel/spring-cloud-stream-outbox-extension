package com.dilaverdemirel.outbox.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode(of = "id")
@Entity
public class OutboxMessage implements Serializable {
    @Id
    @Column(nullable = false)
    private final String id;

    @Column(nullable = false)
    private final String source;

    @Column(nullable = false)
    private final String sourceId;

    @Column(nullable = false)
    private final String channel;

    @Lob
    @Column(nullable = false)
    private final String payload;

    @Column(length = 6, nullable = false)
    @Enumerated(EnumType.STRING)
    private final Status status;

    public enum Status {
        NEW, FAILED, SENT
    }
}
