package com.dilaverdemirel.spring.outbox.service;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.dto.DummyMessagePayload;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.dilaverdemirel.spring.outbox.service.impl.OutboxMessagePublisherServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.QUERY_DELAY_FOR_MESSAGE_THAT_COULD_NOT_BE_SENT;
import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.QUERY_RESULT_PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dilaverdemirel
 * @since 17.05.2020
 */
@ExtendWith(MockitoExtension.class)
public class OutboxMessagePublisherServiceImplTest {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Mock
    private static MessageChannel messageChannel;
    @Mock
    private OutboxMessageRepository outboxMessageRepository;
    @Mock
    private BinderAwareChannelResolver channelResolver;
    @InjectMocks
    private OutboxMessagePublisherServiceImpl outboxMessagePublisherService;

    @Test
    public void testPublishById_it_should_publish_event_when_id_is_valid() throws IOException {
        //Given
        final var id = "message-1";

        final var contentIndex = 1;
        final OutboxMessage mockOutboxMessage = getOutboxMessage(contentIndex);
        final OutboxMessage outboxMessageForVerification = getOutboxMessage(contentIndex);
        final var messagePayload = DummyMessagePayload.builder().id("content-id-1").name("content-name-1").build();

        when(outboxMessageRepository.findById(id)).thenReturn(Optional.of(mockOutboxMessage));

        when(channelResolver.resolveDestination(mockOutboxMessage.getChannel()))
                .thenReturn(messageChannel);

        //When
        outboxMessagePublisherService.publishById(id);

        //Then
        verify(outboxMessageRepository).findById(id);

        final var eventArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageChannel).send(eventArgumentCaptor.capture());
        final var payloadString = (String) eventArgumentCaptor.getValue().getPayload();
        final var capturedDummyMessagePayload = OBJECT_MAPPER.readValue(payloadString, DummyMessagePayload.class);
        assertThat(capturedDummyMessagePayload)
                .isEqualToComparingFieldByField(messagePayload);

        final var savedMessageArgumentCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(savedMessageArgumentCaptor.capture());
        assertThat(savedMessageArgumentCaptor.getValue())
                .isEqualToIgnoringGivenFields(outboxMessageForVerification, "status", "sentAt");
        assertThat(savedMessageArgumentCaptor.getValue().getStatus()).isEqualTo(OutboxMessageStatus.SENT);
        assertThat(savedMessageArgumentCaptor.getValue().getSentAt()).isNotNull();
    }

    @Test
    public void testPublishAllFailedMessages_it_should_publish_when_there_are_some_failed_messages() throws IOException {
        //Given
        final List<OutboxMessage> outboxMessages = getOutboxMessages();

        final var outboxMessagePage = new PageImpl<>(outboxMessages);
        when(outboxMessageRepository.findByStatus(any(OutboxMessageStatus.class), any(Pageable.class))).thenReturn(outboxMessagePage);

        when(channelResolver.resolveDestination(anyString()))
                .thenReturn(messageChannel);

        when(outboxMessageRepository.findMessagesThatCouldNotBeSent(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        //When
        outboxMessagePublisherService.publishAllFailedMessages();

        //Then

        //Find verification
        final var statusForFindArgumentCaptor = ArgumentCaptor.forClass(OutboxMessageStatus.class);
        final var pageableForFindArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(outboxMessageRepository).findByStatus(statusForFindArgumentCaptor.capture(), pageableForFindArgumentCaptor.capture());
        assertThat(statusForFindArgumentCaptor.getValue()).isEqualTo(OutboxMessageStatus.FAILED);

        Assertions.assertThat(pageableForFindArgumentCaptor.getValue())
                .isNotNull()
                .hasFieldOrPropertyWithValue("pageSize", QUERY_RESULT_PAGE_SIZE)
                .hasFieldOrPropertyWithValue("pageNumber", 0);

        Assertions.assertThat(pageableForFindArgumentCaptor.getValue().getSort())
                .isNotNull()
                .containsOnly(new Sort.Order(Sort.Direction.ASC, "createdAt"));

        //Send and save verification
        validateSentMessages(outboxMessages);
    }

    @Test
    public void testPublishAllFailedMessages_it_should_publish_when_there_are_some_messages_could_not_be_sent() throws IOException {
        //Given
        final List<OutboxMessage> outboxMessages = getOutboxMessages();

        final var outboxMessagePage = new PageImpl<OutboxMessage>(Collections.emptyList());
        when(outboxMessageRepository.findByStatus(any(OutboxMessageStatus.class), any(Pageable.class))).thenReturn(outboxMessagePage);

        final var outboxMessagePageThatNotSent = new PageImpl<>(outboxMessages);
        when(outboxMessageRepository.findMessagesThatCouldNotBeSent(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(outboxMessagePageThatNotSent);

        when(channelResolver.resolveDestination(anyString()))
                .thenReturn(messageChannel);

        //When
        outboxMessagePublisherService.publishAllFailedMessages();

        //Then

        //Find verification
        verify(outboxMessageRepository).findByStatus(any(OutboxMessageStatus.class), any(Pageable.class));

        final var delayStartForFindArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        final var pageableForFindArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(outboxMessageRepository)
                .findMessagesThatCouldNotBeSent(
                        delayStartForFindArgumentCaptor.capture(),
                        pageableForFindArgumentCaptor.capture());

        assertThat(delayStartForFindArgumentCaptor.getValue()).isNotNull().isBetween(
                LocalDateTime.now().minus(QUERY_DELAY_FOR_MESSAGE_THAT_COULD_NOT_BE_SENT + 15, ChronoUnit.SECONDS),
                LocalDateTime.now());

        Assertions.assertThat(pageableForFindArgumentCaptor.getValue())
                .isNotNull()
                .hasFieldOrPropertyWithValue("pageSize", QUERY_RESULT_PAGE_SIZE)
                .hasFieldOrPropertyWithValue("pageNumber", 0);

        Assertions.assertThat(pageableForFindArgumentCaptor.getValue().getSort())
                .isNotNull()
                .containsOnly(new Sort.Order(Sort.Direction.ASC, "createdAt"));

        //Send and save verification
        validateSentMessages(outboxMessages);

    }

    private void validateSentMessages(List<OutboxMessage> outboxMessages) {
        final var sentMessagesArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageChannel, times(3)).send(sentMessagesArgumentCaptor.capture());
        final var sentMessagesArgumentCaptorAllValues = sentMessagesArgumentCaptor.getAllValues();

        final var savedMessagesArgumentCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository, times(3)).save(savedMessagesArgumentCaptor.capture());
        final var savedMessagesArgumentCaptorAllValues = savedMessagesArgumentCaptor.getAllValues();

        for (int i = 0; i < outboxMessages.size(); i++) {
            final var outboxMessageForVerification = outboxMessages.get(i);
            final var capturedSentMessage = sentMessagesArgumentCaptorAllValues.get(i);
            final var capturedSavedOutboxMessage = savedMessagesArgumentCaptorAllValues.get(i);

            final var payloadString = (String) capturedSentMessage.getPayload();
            assertThat(payloadString).isEqualTo(outboxMessageForVerification.getPayload());

            assertThat(capturedSavedOutboxMessage)
                    .isEqualToIgnoringGivenFields(outboxMessageForVerification, "status", "sentAt");
            assertThat(savedMessagesArgumentCaptor.getValue().getStatus()).isEqualTo(OutboxMessageStatus.SENT);
            assertThat(savedMessagesArgumentCaptor.getValue().getSentAt()).isNotNull();
        }
    }

    private List<OutboxMessage> getOutboxMessages() {
        return Arrays.asList(
                getOutboxMessage(1),
                getOutboxMessage(2),
                getOutboxMessage(3));
    }

    private OutboxMessage getOutboxMessage(int contentIndex) {
        return OutboxMessage.builder()
                .status(OutboxMessageStatus.FAILED)
                .payload(String.format("{\"id\":\"content-id-%d\",\"name\":\"content-name-%d\"}", contentIndex, contentIndex))
                .channel("channel-1")
                .build();
    }
}
