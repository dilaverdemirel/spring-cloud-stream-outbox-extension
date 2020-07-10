package com.dilaverdemirel.spring.outbox.util;


import org.springframework.messaging.Message;

import java.util.Objects;

import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME;
import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
public final class MessageUtils {
    private MessageUtils() {
    }

    public static String extractOutboxMessageId(Message message) {
        final var messageIdObject = message.getHeaders().get(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME);
        if (Objects.nonNull(messageIdObject)) {
            return (String) messageIdObject;
        }

        return null;
    }

    public static String extractExceptionStackTrace(Message message) {
        final var exceptionObject = message.getHeaders().get(OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME);
        if (Objects.nonNull(exceptionObject)) {
            return (String) exceptionObject;
        }

        return null;
    }
}
