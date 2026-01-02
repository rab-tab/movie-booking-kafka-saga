package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = { SEAT_RESERVED_TOPIC }
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class BookingKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private BookingService bookingService;

    @Test
    void should_consume_seat_reserved_event_and_mark_pending() {

        SeatReservedEvent event =
                new SeatReservedEvent("B1", true,200);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(bookingService).markBookingPending("B1")
                );
    }
}
