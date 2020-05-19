package com.dilaverdemirel.spring.outbox.listener;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.dto.DummyMessagePayload;
import com.dilaverdemirel.spring.outbox.dto.OutboxMessageEvent;
import com.dilaverdemirel.spring.outbox.dto.OutboxMessageEventMetaData;
import com.dilaverdemirel.spring.outbox.exception.OutboxMessageValidationException;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@ExtendWith(MockitoExtension.class)
class OutboxMessageHandlerTest {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private OutboxMessageHandler outboxMessageHandler;

    private static Stream<OutboxMessageEvent> provideEventsForValidationException() {
        return Stream.of(
                OutboxMessageEvent.builder()
                        .sourceId("src-id-1").payload("payload").channel("chn-1").build(),
                OutboxMessageEvent.builder()
                        .source("src").payload("payload").channel("chn-1").build(),
                OutboxMessageEvent.builder()
                        .sourceId("src-id-1").source("src").channel("chn-1").build(),
                OutboxMessageEvent.builder()
                        .sourceId("src-id-1").source("src").payload("payload").build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideEventsForValidationException")
    public void testOnOutboxMessageCreate_it_should_throw_validation_exception_when_any_field_of_event_is_empty(OutboxMessageEvent event) {
        final OutboxMessageValidationException exception =
                assertThrows(
                        OutboxMessageValidationException.class,
                        () -> outboxMessageHandler.onOutboxMessageCreate(event));
        assertEquals("Please enter all fields data for outbox message send!", exception.getMessage());
    }

    @Test
    public void testOnOutboxMessageCreate_it_should_save_and_publish_meta_event_message_when_event_is_valid() throws IOException {
        //Given
        final var messagePayload = getDummyMessagePayload();
        final var outboxMessageEvent = getOutboxMessageEvent(messagePayload);
        final var mockOutboxMessage = OutboxMessage.builder().id("message-1").build();
        when(outboxMessageRepository.save(any(OutboxMessage.class))).thenReturn(mockOutboxMessage);

        //When
        outboxMessageHandler.onOutboxMessageCreate(outboxMessageEvent);

        //Then
        final var publishedEventArgumentCaptor = ArgumentCaptor.forClass(OutboxMessageEventMetaData.class);
        verify(applicationEventPublisher).publishEvent(publishedEventArgumentCaptor.capture());
        assertThat(publishedEventArgumentCaptor.getValue().getMessageId()).isEqualTo(mockOutboxMessage.getId());

        final var savedMessageArgumentCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(savedMessageArgumentCaptor.capture());

        final var messagePayloadJson = OBJECT_MAPPER.writeValueAsString(messagePayload);
        final var capturedMessageSaveValue = savedMessageArgumentCaptor.getValue();
        assertThat(capturedMessageSaveValue)
                .isEqualToIgnoringGivenFields(outboxMessageEvent,
                        "id",
                        "payload",
                        "status",
                        "messageClass",
                        "createdAt",
                        "sentAt");

        assertThat(capturedMessageSaveValue.getId()).isNotBlank();
        assertThat(capturedMessageSaveValue.getStatus()).isEqualTo(OutboxMessageStatus.NEW);
        assertThat(capturedMessageSaveValue.getPayload()).isEqualTo(messagePayloadJson);
        assertThat(capturedMessageSaveValue.getCreatedAt()).isNotNull();
        assertThat(capturedMessageSaveValue.getMessageClass()).isEqualTo(DummyMessagePayload.class.getName());
    }

    private OutboxMessageEvent getOutboxMessageEvent(DummyMessagePayload messagePayload) {
        return OutboxMessageEvent.builder()
                .source("src-1").sourceId("src-id-1").payload(messagePayload).channel("chn-1").build();
    }

    private DummyMessagePayload getDummyMessagePayload() {
        return DummyMessagePayload.builder().id("id-1").name("name-1").build();
    }
}