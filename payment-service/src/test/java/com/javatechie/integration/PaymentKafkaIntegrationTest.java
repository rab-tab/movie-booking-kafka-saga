package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.PaymentService;
import org.awaitility.Awaitility;
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
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class PaymentKafkaIntegrationTest {

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:8.9.0"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @SpyBean
    private PaymentService paymentService;

    @Test
    void should_consume_seat_reserved_event_and_process_payment() {
        SeatReservedEvent event =
                new SeatReservedEvent("B10", true, 1000);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        verify(paymentService).processPayment(any())
                );
    }
}

