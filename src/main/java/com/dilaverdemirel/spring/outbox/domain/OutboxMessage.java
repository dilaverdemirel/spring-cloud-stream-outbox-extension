package com.dilaverdemirel.spring.outbox.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OutboxMessage implements Serializable {
    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String sourceId;

    @Column(nullable = false)
    private String channel;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(length = 6, nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxMessageStatus status;

    @Column(nullable = false)
    private String messageClass;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Integer retryCount;

    @Column
    private String statusMessage;

    private LocalDateTime sentAt;

}
