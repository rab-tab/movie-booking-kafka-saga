package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


import java.time.Duration;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        topics = SEAT_RESERVED_TOPIC,
        partitions = 1
)
class BookingKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private BookingService bookingService;

    @Test
    void should_consume_seat_reserved_event_and_mark_pending() {

        SeatReservedEvent event = new SeatReservedEvent("B1", true, 200);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        verify(bookingService).markBookingPending("B1")
                );
    }
}

