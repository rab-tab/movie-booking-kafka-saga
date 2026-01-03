package com.javatechie.service;

import com.javatechie.PaymentEventsProducer;
import com.javatechie.events.BookingPaymentEvent;
import com.javatechie.events.SeatReservedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private PaymentEventsProducer eventsProducer;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void should_publish_success_event_when_amount_is_valid() {
        SeatReservedEvent event =
                new SeatReservedEvent("B1", true, 500);

        paymentService.processPayment(event);

        verify(kafkaTemplate).send(
                eq("payment-events"),
                any(BookingPaymentEvent.class)
        );

        verify(eventsProducer).publishPaymentSuccessEvent(event);
        verify(eventsProducer, never()).publishPaymentFailureEvent(any());
    }

    @Test
    void should_publish_failure_event_when_amount_exceeds_limit() {
        SeatReservedEvent event =
                new SeatReservedEvent("B2", true, 3000);

        paymentService.processPayment(event);

        verify(eventsProducer).publishPaymentFailureEvent(event);
        verify(eventsProducer, never()).publishPaymentSuccessEvent(any());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
}

