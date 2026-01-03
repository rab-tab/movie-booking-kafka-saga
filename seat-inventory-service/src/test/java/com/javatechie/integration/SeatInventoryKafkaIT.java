package com.javatechie.integration;


import com.javatechie.entity.SeatInventory;
import com.javatechie.events.BookingCreatedEvent;
import com.javatechie.repository.SeatInventoryRepository;
import com.javatechie.utils.enums.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class SeatInventoryKafkaIT {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer("confluentinc/cp-kafka:7.5.1");

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private SeatInventoryRepository repository;

    @Test
    void should_consume_booking_event_and_lock_seats() {

        SeatInventory seat =
                SeatInventory.builder()
                        .showId("show1")
                        .seatNumber("A1")
                        .status(SeatStatus.AVAILABLE)
                        .build();

        repository.save(seat);

        BookingCreatedEvent event =
                new BookingCreatedEvent("b1", "user1","show1", List.of("A1"), 300);

        kafkaTemplate.send("movie-booking-events", "b1", event);

        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    SeatInventory updated =
                            repository.findAll().get(0);
                    assertEquals(SeatStatus.LOCKED, updated.getStatus());
                    assertEquals("b1", updated.getCurrentBookingId());
                });
    }
}
