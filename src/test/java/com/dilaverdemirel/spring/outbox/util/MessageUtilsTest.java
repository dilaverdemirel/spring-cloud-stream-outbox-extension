package com.dilaverdemirel.spring.outbox.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME;
import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
@ExtendWith(MockitoExtension.class)
public class MessageUtilsTest {
    @Test
    public void testExtractOutboxMessageId_it_should_return_message_id_when_message_has_an_id() {
        //Given
        final var messageId = "message-id";

        //When
        final var extractedMessageId = MessageUtils.extractOutboxMessageId(Map.of(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME, messageId));

        //Then
        assertEquals(extractedMessageId, messageId);
    }

    @Test
    public void testExtractOutboxMessageId_it_should_return_null_when_message_don_t_has_an_id() {
        //Given

        //When
        final var extractedMessageId = MessageUtils.extractOutboxMessageId(Map.of());

        //Then
        assertNull(extractedMessageId);
    }

    @Test
    public void testExtractExceptionStackTrace_it_should_return_stack_stace_when_message_has_an_exception() {
        //Given
        final var stackTrace = "stack-trace";

        //When
        final var extractedMessageId = MessageUtils
                .extractExceptionStackTrace(Map.of(OUTBOX_MESSAGE_EXCEPTION_HEADER_PARAMETER_NAME, stackTrace));

        //Then
        assertEquals(extractedMessageId, stackTrace);
    }

    @Test
    public void testExtractExceptionStackTrace_it_should_return_null_when_message_don_t_has_an_exception() {
        //Given

        //When
        final var extractedMessageId = MessageUtils.extractExceptionStackTrace(Map.of());

        //Then
        assertNull(extractedMessageId);
    }
}