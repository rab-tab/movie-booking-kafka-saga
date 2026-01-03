package com.javatechie.integration;


import com.javatechie.entity.SeatInventory;
import com.javatechie.events.BookingPaymentEvent;
import com.javatechie.repository.SeatInventoryRepository;
import com.javatechie.utils.enums.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentStatusListenerIT {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    SeatInventoryRepository repository;

    @Test
    void should_release_seats_on_payment_failure_event() {

        SeatInventory seat =
                SeatInventory.builder()
                        .status(SeatStatus.LOCKED)
                        .currentBookingId("b1")
                        .build();

        repository.save(seat);

        kafkaTemplate.send(
                "payment-events",
                "b1",
                new BookingPaymentEvent("b1", false, 400)
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    SeatInventory updated =
                            repository.findAll().get(0);
                    assertEquals(SeatStatus.AVAILABLE, updated.getStatus());
                    assertNull(updated.getCurrentBookingId());
                });
    }
}

