package com.javatechie.integration;

import com.javatechie.entity.SeatInventory;
import com.javatechie.events.BookingCreatedEvent;
import com.javatechie.repository.SeatInventoryRepository;
import com.javatechie.utils.enums.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = { "movie-booking-events" },
        brokerProperties = { "listeners=PLAINTEXT://localhost:0" } // random port
)
@ActiveProfiles("test")
class SeatInventoryKafkaIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private SeatInventoryRepository repository;

    @Test
    void should_consume_booking_event_and_lock_seats() {

        // GIVEN: a seat available in the repository
        SeatInventory seat = SeatInventory.builder()
                .showId("show1")
                .seatNumber("A1")
                .status(SeatStatus.AVAILABLE)
                .build();

        repository.save(seat);

        // WHEN: publish booking created event
        BookingCreatedEvent event = new BookingCreatedEvent(
                "b1",
                "user1",
                "show1",
                List.of("A1"),
                300
        );

        kafkaTemplate.send("movie-booking-events", event.bookingId(), event);
        kafkaTemplate.flush();

        // THEN: await until the consumer updates the seat status
        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    SeatInventory updated = repository.findAll().get(0);
                    assertEquals(SeatStatus.LOCKED, updated.getStatus());
                    assertEquals("b1", updated.getCurrentBookingId());
                });
    }
}
