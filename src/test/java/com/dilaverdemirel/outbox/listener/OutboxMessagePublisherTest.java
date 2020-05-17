package com.dilaverdemirel.outbox.listener;

import com.dilaverdemirel.outbox.dto.OutboxMessageEventMetaData;
import com.dilaverdemirel.outbox.service.OutboxMessagePublisherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@ExtendWith(MockitoExtension.class)
public class OutboxMessagePublisherTest {
    @Mock
    private OutboxMessagePublisherService outboxMessagePublisherService;

    @InjectMocks
    private OutboxMessagePublisher outboxMessagePublisher;

    @Test
    public void testOnOutboxMessageSave_it_should_call_publish_by_id_when_message_received() {
        //Given
        final var messageId = "message-1";

        //When
        outboxMessagePublisher.onOutboxMessageSave(OutboxMessageEventMetaData.builder().messageId(messageId).build());

        //Then
        verify(outboxMessagePublisherService).publishById(messageId);
    }
}
