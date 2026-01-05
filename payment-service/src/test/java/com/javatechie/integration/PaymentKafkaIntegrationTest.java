package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.PaymentService;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {SEAT_RESERVED_TOPIC},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0"}
)
@ActiveProfiles("test")
class PaymentKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private PaymentService paymentService;

    @Disabled
    @Test
    void should_process_payment_for_reserved_seat() {

        SeatReservedEvent event = new SeatReservedEvent("B100", true, 1000);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(paymentService, atLeastOnce())
                                .processPayment(any(SeatReservedEvent.class))
                );
    }

    @Test
    void should_skip_payment_for_not_reserved_seat() {

        SeatReservedEvent event = new SeatReservedEvent("B101", false, 500);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(paymentService, never())
                                .processPayment(any())
                );
    }
}
