package com.dilaverdemirel.spring.outbox.scheduler;

import com.dilaverdemirel.spring.outbox.service.OutboxMessagePublisherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author dilaverdemirel
 * @since 30.05.2020
 */
@ExtendWith(MockitoExtension.class)
public class FailedOutboxMessageSchedulerServiceTest {

    @Mock
    private OutboxMessagePublisherService outboxMessagePublisherService;

    @InjectMocks
    private FailedOutboxMessageSchedulerService failedOutboxMessageSchedulerService;

    @Test
    public void testSendFailedMessages_it_should_call_resend_method_when_the_job_is_running() {
        //Given

        //When
        failedOutboxMessageSchedulerService.sendFailedMessages();

        //Then
        Mockito.verify(outboxMessagePublisherService).publishAllFailedMessages();
    }

}