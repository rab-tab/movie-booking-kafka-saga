package com.javatechie.service;

import com.javatechie.PaymentEventsProducer;
import com.javatechie.events.BookingPaymentEvent;
import com.javatechie.events.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.javatechie.common.KafkaConfigProperties.PAYMENT_EVENTS_TOPIC;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentEventsProducer eventsProducer;

    public void processPayment(SeatReservedEvent event) {

        log.info("💳 Processing payment for bookingId={}", event.bookingId());

        if (event.amount() > 2000) {
            log.info("❌ Payment amount exceeds limit for bookingId={}", event.bookingId());
            eventsProducer.publishPaymentFailureEvent(event);
            return;
        }

        kafkaTemplate.send(
                PAYMENT_EVENTS_TOPIC,
                new BookingPaymentEvent(event.bookingId(), true, event.amount())
        );

        eventsProducer.publishPaymentSuccessEvent(event);
        log.info("✅ Payment successful for bookingId={}", event.bookingId());
    }
}
