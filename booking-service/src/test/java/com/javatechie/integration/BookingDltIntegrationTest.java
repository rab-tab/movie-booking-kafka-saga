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
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;

import static com.javatechie.common.KafkaConfigProperties.SEAT_RESERVED_TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = { SEAT_RESERVED_TOPIC, SEAT_RESERVED_TOPIC + "-dlt" }
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class BookingDltIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @MockBean
    private BookingService bookingService;

    @Test
    void should_send_message_to_dlt_after_retries() {

        // 🔥 Force retryable failure
        doThrow(new RuntimeException("DB down"))
                .when(bookingService)
                .markBookingPending(any());

        SeatReservedEvent event =
                new SeatReservedEvent("B2", true,100);

        kafkaTemplate.send(SEAT_RESERVED_TOPIC, event.bookingId(), event);

        Consumer<String, Object> consumer =
                consumerFactory.createConsumer("test-dlt-group", "");

        consumer.subscribe(List.of(SEAT_RESERVED_TOPIC + "-dlt"));

        ConsumerRecords<String, Object> records =
                KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15));

        assertThat(records.count()).isGreaterThan(0);
    }
}
