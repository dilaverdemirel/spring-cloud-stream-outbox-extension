package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.domain.OutboxMessageStatus;
import com.dilaverdemirel.spring.outbox.exception.OutboxMessageNotFoundException;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author dilaverdemirel
 * @since 8.07.2020
 */
@ExtendWith(MockitoExtension.class)
public class FailedOutboxMessageServiceImplTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private FailedOutboxMessageServiceImpl failedOutboxMessageService;

    @Test
    public void testMarkAsFailed_it_should_set_status_as_FAILED_when_there_is_a_valid_outbox_message() {
        //Given
        final var id = "message-id";
        final var exception = "exception-message";
        final var mockOutboxMessage = getOutboxMessage(1);
        when(outboxMessageRepository.findById(id)).thenReturn(Optional.of(mockOutboxMessage));

        //When
        failedOutboxMessageService.markAsFailedWithExceptionMessage(id, exception);

        //Then
        final var savedMessageAC = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(savedMessageAC.capture());

        assertThat(savedMessageAC.getValue().getStatus())
                .isEqualTo(OutboxMessageStatus.FAILED);
        assertThat(savedMessageAC.getValue().getStatusMessage())
                .isEqualTo(exception);


        verify(outboxMessageRepository).findById(id);
    }

    @Test
    public void testMarkAsFailed_it_should_throw_exception_when_there_is_no_a_valid_outbox_message() {
        //Given
        final var id = "message-id";
        when(outboxMessageRepository.findById(id)).thenReturn(Optional.empty());

        //For expected exception
        assertThrows(OutboxMessageNotFoundException.class, () -> failedOutboxMessageService
                .markAsFailedWithExceptionMessage(id, "message"));


        verify(outboxMessageRepository).findById(id);
    }

    @Test
    public void testMarkAsFailed_it_should_do_nothing_when_id_is_blank() {
        //Given
        final var id = "";

        //When
        failedOutboxMessageService.markAsFailedWithExceptionMessage(id, "message");

        //Then
        verifyZeroInteractions(outboxMessageRepository);
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
