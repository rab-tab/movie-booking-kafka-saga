package com.javatechie.service;

import com.javatechie.config.BookingStatus;
import com.javatechie.entity.Booking;
import com.javatechie.messaging.BookingEventProducer;
import com.javatechie.repository.BookingRepository;
import com.javatechie.request.BookingRequest;
import com.javatechie.response.BookingResponse;
import com.javatechie.service.BookingService;
import com.javatechie.utils.mapper.BookingRequestToEntityMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingEventProducer bookingEventProducer;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void should_book_seats_and_publish_event() {
        BookingRequest request = new BookingRequest(
                "res-123",                 // reservationId
                "show-1",                  // showId
                List.of("A1"),              // seatIds
                "user-1",                  // userId
                Instant.now(),              // timestamp
                200L                        // amount
        );

        Booking savedBooking = BookingRequestToEntityMapper.map(request);
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        BookingResponse response = bookingService.bookSeats(request);

        assertNotNull(response);
        verify(bookingRepository).save(any());
        verify(bookingEventProducer).publishBookingEvents(any());
    }

    @Test
    void should_mark_booking_confirmed() {
        Booking booking = new Booking();
        booking.setBookingCode("B1");
        booking.setStatus(BookingStatus.PENDING);

        when(bookingRepository.findByBookingCode("B1")).thenReturn(booking);

        bookingService.markBookingConfirmed("B1");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void should_not_confirm_already_confirmed_booking() {
        Booking booking = new Booking();
        booking.setBookingCode("B1");
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingCode("B1")).thenReturn(booking);

        bookingService.markBookingConfirmed("B1");

        verify(bookingRepository, never()).save(any());
    }
}
