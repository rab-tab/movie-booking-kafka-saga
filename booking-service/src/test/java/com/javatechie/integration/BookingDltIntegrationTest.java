package com.javatechie.integration;

import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {
                SEAT_RESERVED_TOPIC,
                SEAT_RESERVED_TOPIC + "-dlt"
        }
)
class BookingDltIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockBean
    private BookingService bookingService;

    private Consumer<String, Object> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void should_send_event_to_dlt_after_retries() {

        // 🔥 Force retry exhaustion
        doThrow(new RuntimeException("DB down"))
                .when(bookingService)
                .markBookingPending(any());

        SeatReservedEvent event =
                new SeatReservedEvent("B2", true, 500);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        consumer = consumerFactory.createConsumer(
                "dlt-test-group",
                "dlt-test-client"
        );

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(
                consumer,
                SEAT_RESERVED_TOPIC + "-dlt"
        );

        ConsumerRecords<String, Object> records =
                KafkaTestUtils.getRecords(
                        consumer,
                        Duration.ofSeconds(20)
                );

        assertThat(records.count())
                .as("DLT should receive failed message")
                .isGreaterThan(0);
    }
}
