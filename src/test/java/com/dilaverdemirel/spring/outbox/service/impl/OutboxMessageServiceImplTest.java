package com.dilaverdemirel.spring.outbox.service.impl;

import com.dilaverdemirel.spring.outbox.domain.OutboxMessage;
import com.dilaverdemirel.spring.outbox.repository.OutboxMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dilaverdemirel
 * @since 13/01/2022
 */
@ExtendWith(MockitoExtension.class)
public class OutboxMessageServiceImplTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private OutboxMessageServiceImpl outboxMessageService;

    @Test
    public void testGetById_it_should_get_and_return_outbox_message_by_id() {
        //Given
        final var id = "ID-001";
        final var mockOutboxMessage = new OutboxMessage();
        mockOutboxMessage.setId(id);
        mockOutboxMessage.setPayload("Payload");
        when(outboxMessageRepository.findById(id))
                .thenReturn(Optional.of(mockOutboxMessage));

        //When
        final var resultOpt = outboxMessageService.getById(id);

        //Then
        verify(outboxMessageRepository).findById(id);
        assertThat(resultOpt).isPresent();
        assertThat(resultOpt.get()).isEqualToComparingFieldByField(mockOutboxMessage);
    }

    @Test
    public void testDeleteOldOutboxMessages_it_should_delete_old_messages() {
        //Given
        final var thresholdDate = LocalDateTime.now();
        final var mockDeletedRecordCount = 100L;
        when(outboxMessageRepository.deleteOldOutboxMessages(thresholdDate)).thenReturn(mockDeletedRecordCount);

        //When
        final var deletedRecordCount = outboxMessageService.deleteOldOutboxMessages(thresholdDate);

        //Then
        assertThat(deletedRecordCount).isEqualTo(mockDeletedRecordCount);
        verify(outboxMessageRepository).deleteOldOutboxMessages(thresholdDate);
    }
}