package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.dto.DummyMessagePayload;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService.OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME;
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

    private final Integer defaultRetryCountThreshold = 3;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private BinderAwareChannelResolver channelResolver;

    @InjectMocks
    private OutboxMessagePublisherServiceImpl outboxMessagePublisherService;

    @BeforeEach
    public void beforeEach() {
        outboxMessagePublisherService.retryCountThreshold = defaultRetryCountThreshold;
    }

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
        final var messageIdHeader = eventArgumentCaptor.getValue().getHeaders().get(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME);
        assertThat(messageIdHeader).isEqualTo("outbox-id");

        final var savedMessageArgumentCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(savedMessageArgumentCaptor.capture());
        assertThat(savedMessageArgumentCaptor.getValue())
                .isEqualToIgnoringGivenFields(outboxMessageForVerification, "status", "sentAt", "retryCount");
        assertThat(savedMessageArgumentCaptor.getValue().getStatus()).isEqualTo(OutboxMessageStatus.SENT);
        assertThat(savedMessageArgumentCaptor.getValue().getRetryCount()).isEqualTo(0);
        assertThat(savedMessageArgumentCaptor.getValue().getSentAt()).isNotNull();
    }

    @Test
    public void testPublishAllFailedMessages_it_should_publish_when_there_are_some_failed_messages() throws IOException {
        //Given
        final List<OutboxMessage> outboxMessages = getOutboxMessages();

        final var outboxMessagePage = new PageImpl<>(outboxMessages);
        when(outboxMessageRepository
                .findByStatusAndRetryCountLessThanEqual(any(OutboxMessageStatus.class), any(Integer.class), any(Pageable.class)))
                .thenReturn(outboxMessagePage);

        when(channelResolver.resolveDestination(anyString()))
                .thenReturn(messageChannel);

        when(outboxMessageRepository.findMessagesThatCouldNotBeSent(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        //When
        outboxMessagePublisherService.publishAllFailedMessages();

        //Then

        //Find verification
        final var statusForFindArgumentCaptor = ArgumentCaptor.forClass(OutboxMessageStatus.class);
        final var retryCountThresholdAC = ArgumentCaptor.forClass(Integer.class);
        final var pageableForFindArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(outboxMessageRepository)
                .findByStatusAndRetryCountLessThanEqual(statusForFindArgumentCaptor.capture(), retryCountThresholdAC.capture(),
                        pageableForFindArgumentCaptor.capture());
        assertThat(statusForFindArgumentCaptor.getValue()).isEqualTo(OutboxMessageStatus.FAILED);
        assertThat(retryCountThresholdAC.getValue()).isEqualTo(defaultRetryCountThreshold);

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
    public void testPublishAllFailedMessages_it_should_publish_when_there_are_some_messages_could_not_be_sent() {
        //Given
        final List<OutboxMessage> outboxMessages = getOutboxMessages();

        final var outboxMessagePage = new PageImpl<OutboxMessage>(Collections.emptyList());
        when(outboxMessageRepository
                .findByStatusAndRetryCountLessThanEqual(any(OutboxMessageStatus.class), any(Integer.class), any(Pageable.class)))
                .thenReturn(outboxMessagePage);

        final var outboxMessagePageThatNotSent = new PageImpl<>(outboxMessages);
        when(outboxMessageRepository.findMessagesThatCouldNotBeSent(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(outboxMessagePageThatNotSent);

        when(channelResolver.resolveDestination(anyString()))
                .thenReturn(messageChannel);

        //When
        outboxMessagePublisherService.publishAllFailedMessages();

        //Then

        //Find verification
        verify(outboxMessageRepository).findByStatusAndRetryCountLessThanEqual(any(OutboxMessageStatus.class), any(Integer.class), any(Pageable.class));

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
                    .isEqualToIgnoringGivenFields(outboxMessageForVerification, "status", "sentAt", "retryCount");
            assertThat(savedMessagesArgumentCaptor.getValue().getStatus()).isEqualTo(OutboxMessageStatus.SENT);
            assertThat(savedMessagesArgumentCaptor.getValue().getRetryCount()).isEqualTo(4);
            assertThat(savedMessagesArgumentCaptor.getValue().getSentAt()).isNotNull();

            final var messageIdHeader = capturedSentMessage.getHeaders().get(OUTBOX_MESSAGE_ID_HEADER_PARAMETER_NAME);
            assertThat(messageIdHeader).isEqualTo("outbox-id");
        }
    }

    private List<OutboxMessage> getOutboxMessages() {
        final var outboxMessage1 = getOutboxMessage(1);
        outboxMessage1.setRetryCount(3);
        final var outboxMessage2 = getOutboxMessage(2);
        outboxMessage2.setRetryCount(3);
        final var outboxMessage3 = getOutboxMessage(3);
        outboxMessage3.setRetryCount(3);

        return Arrays.asList(
                outboxMessage1,
                outboxMessage2,
                outboxMessage3);
    }

    private OutboxMessage getOutboxMessage(int contentIndex) {
        return OutboxMessage.builder()
                .id("outbox-id")
                .status(OutboxMessageStatus.FAILED)
                .payload(String.format("{\"id\":\"content-id-%d\",\"name\":\"content-name-%d\"}", contentIndex, contentIndex))
                .channel("channel-1")
                .retryCount(-1)
                .build();
    }
}
