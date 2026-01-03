package com.javatechie.listener;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeatReserveEventConsumerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private SeatReserveEventConsumer consumer;

    @Test
    void should_process_payment_when_seat_is_reserved() {
        SeatReservedEvent event =
                new SeatReservedEvent("B1", true, 800);

        consumer.consume(event);

        verify(paymentService).processPayment(event);
    }

    @Test
    void should_skip_payment_when_seat_not_reserved() {
        SeatReservedEvent event =
                new SeatReservedEvent("B2", false, 800);

        consumer.consume(event);

        verify(paymentService, never()).processPayment(any());
    }
}
