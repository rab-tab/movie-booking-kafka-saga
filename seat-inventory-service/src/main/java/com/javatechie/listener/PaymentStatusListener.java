package com.javatechie.listener;

import com.javatechie.common.KafkaConfigProperties;
import com.javatechie.events.BookingPaymentEvent;
import com.javatechie.service.BookingService;
import com.javatechie.service.SeatInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.javatechie.common.KafkaConfigProperties.PAYMENT_EVENTS_TOPIC;
import static com.javatechie.common.KafkaConfigProperties.SEAT_EVENT_GROUP;

@Component
@Slf4j

public class PaymentStatusListener {

    private final SeatInventoryService seatInventoryService;

    public PaymentStatusListener(SeatInventoryService seatInventoryService) {
        this.seatInventoryService = seatInventoryService;
    }

    @KafkaListener(
            id = "seat-payment-status-listener",
            topics = PAYMENT_EVENTS_TOPIC,
            groupId = SEAT_EVENT_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentStatusEvents(BookingPaymentEvent event) {

        log.info("PaymentStatusListener:: Consuming payment event {}", event.bookingId());

        if (!event.paymentCompleted()) {
            log.info("Payment failed for bookingId: {}, releasing seats", event.bookingId());
            seatInventoryService.releaseSeatsOnPaymentFailure(event.bookingId());
        }
        // ✅ Payment success → do NOTHING here
    }
}

