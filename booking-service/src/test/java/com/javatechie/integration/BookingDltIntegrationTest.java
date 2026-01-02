package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@SpringBootTest
class BookingDltIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer("confluentinc/cp-kafka:8.9.0");

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @MockBean
    private BookingService bookingService;

    @Test
    void should_send_event_to_dlt_after_retries() {

        // Force a retryable exception
        doThrow(new RuntimeException("DB down"))
                .when(bookingService)
                .markBookingPending(any());

        SeatReservedEvent event = new SeatReservedEvent("B2", true);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        Consumer<String, Object> consumer =
                consumerFactory.createConsumer("test-dlt-group", "");

        consumer.subscribe(List.of(SEAT_RESERVED_TOPIC + "-dlt"));

        ConsumerRecords<String, Object> records =
                org.springframework.kafka.test.utils.KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15));

        assertThat(records.count()).isGreaterThan(0);
    }
}
