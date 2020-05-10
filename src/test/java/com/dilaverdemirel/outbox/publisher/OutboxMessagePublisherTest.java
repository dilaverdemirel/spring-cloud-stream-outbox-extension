package com.dilaverdemirel.outbox.publisher;

import com.dilaverdemirel.outbox.domain.OutboxMessage;
import com.dilaverdemirel.outbox.dto.DummyMessagePayload;
import com.dilaverdemirel.outbox.dto.OutboxMessageEvent;
import com.dilaverdemirel.outbox.exception.OutboxMessageValidationException;
import com.dilaverdemirel.outbox.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dilaverdemirel
 * @since 10.05.2020
 */
@ExtendWith(MockitoExtension.class)
class OutboxMessagePublisherTest {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private static MessageChannel messageChannel;

    @Mock
    private BinderAwareChannelResolver channelResolver;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private OutboxMessagePublisher outboxMessagePublisher;

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
    public void testOnOutboxMessageSent_it_should_throw_validation_exception_when_any_field_of_event_is_empty(OutboxMessageEvent event) {
        final OutboxMessageValidationException exception =
                assertThrows(
                        OutboxMessageValidationException.class,
                        () -> outboxMessagePublisher.onOutboxMessageSent(event));
        assertEquals("Please enter all fields data for outbox message send!", exception.getMessage());
    }

    @Test
    public void testOnOutboxMessageSent_it_should_send_message_when_event_is_valid() throws IOException {
        //Given
        final var messagePayload = getDummyMessagePayload();
        final var outboxMessageEvent = getOutboxMessageEvent(messagePayload);

        when(channelResolver.resolveDestination(outboxMessageEvent.getChannel()))
                .thenReturn(messageChannel);

        //When
        outboxMessagePublisher.onOutboxMessageSent(outboxMessageEvent);

        //Then
        verify(channelResolver).resolveDestination(outboxMessageEvent.getChannel());

        final var eventArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageChannel).send(eventArgumentCaptor.capture());
        final var payloadString = (String) eventArgumentCaptor.getValue().getPayload();
        final var capturedDummyMessagePayload = OBJECT_MAPPER.readValue(payloadString, DummyMessagePayload.class);
        assertThat(capturedDummyMessagePayload)
                .isEqualToComparingFieldByField(messagePayload);
    }

    @Test
    public void testOnOutboxMessageSent_it_should_save_message_when_any_exception_occurred() throws IOException {
        //Given
        final var messagePayload = getDummyMessagePayload();
        final var outboxMessageEvent = getOutboxMessageEvent(messagePayload);

        when(channelResolver.resolveDestination(outboxMessageEvent.getChannel())).thenThrow(new RuntimeException());

        //When
        outboxMessagePublisher.onOutboxMessageSent(outboxMessageEvent);

        //Then
        verify(channelResolver).resolveDestination(outboxMessageEvent.getChannel());


        final var savedMessageArgumentCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(savedMessageArgumentCaptor.capture());

        final var messagePayloadJson = OBJECT_MAPPER.writeValueAsString(messagePayload);
        final var capturedMessageSaveValue = savedMessageArgumentCaptor.getValue();
        assertThat(capturedMessageSaveValue)
                .isEqualToIgnoringGivenFields(outboxMessageEvent, "id", "payload", "status");

        assertThat(capturedMessageSaveValue.getId()).isNotBlank();
        assertThat(capturedMessageSaveValue.getStatus()).isEqualTo(OutboxMessage.Status.NEW);
        assertThat(capturedMessageSaveValue.getPayload()).isEqualTo(messagePayloadJson);
    }

    private OutboxMessageEvent getOutboxMessageEvent(DummyMessagePayload messagePayload) {
        return OutboxMessageEvent.builder()
                .source("src-1").sourceId("src-id-1").payload(messagePayload).channel("chn-1").build();
    }

    private DummyMessagePayload getDummyMessagePayload() {
        return DummyMessagePayload.builder().id("id-1").name("name-1").build();
    }
}