package com.javatechie.messaging;

import com.javatechie.events.BookingCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static com.javatechie.common.KafkaConfigProperties.MOVIE_BOOKING_EVENTS_TOPIC;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private BookingEventProducer producer;

    @Test
    void should_publish_booking_created_event() {
        BookingCreatedEvent event =
                new BookingCreatedEvent("B1", "U1", "S1", List.of("A1"), 200);

        producer.publishBookingEvents(event);

        verify(kafkaTemplate)
                .send(MOVIE_BOOKING_EVENTS_TOPIC, "B1", event);
    }
}

