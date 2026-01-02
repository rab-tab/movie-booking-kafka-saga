package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class BookingKafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:8.9.0");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private BookingService bookingService;

    @Test
    void should_consume_seat_reserved_event_and_mark_pending() {

        SeatReservedEvent event = new SeatReservedEvent("B1", true);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        verify(bookingService).markBookingPending("B1")
                );
    }
}
