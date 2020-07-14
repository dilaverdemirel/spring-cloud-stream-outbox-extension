package com.dilaverdemirel.spring.outbox.util;


import java.util.Map;
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

    public static String extractOutboxMessageId(Map<String, Object> messageHeaders) {
        final var messageIdObject = messageHeaders.get(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME);
        if (Objects.nonNull(messageIdObject)) {
            return (String) messageIdObject;
        }

        return null;
    }

    public static String extractExceptionStackTrace(Map<String, Object> messageHeaders) {
        final var exceptionObject = messageHeaders.get(OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME);
        if (Objects.nonNull(exceptionObject)) {
            return exceptionObject.toString();
        }

        return null;
    }
}
