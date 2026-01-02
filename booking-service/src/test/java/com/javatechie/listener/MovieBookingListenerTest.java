package com.javatechie.listener;

import com.javatechie.MovieBookingListener;
import com.javatechie.events.SeatReservedEvent;
import com.javatechie.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MovieBookingListenerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private MovieBookingListener listener;

    @Test
    void should_mark_pending_when_seat_reserved() {
        SeatReservedEvent event =
                new SeatReservedEvent("B1", true,500);

        listener.consumeSeatReserveEvents(event);

        verify(bookingService).markBookingPending("B1");
        verify(bookingService, never()).handleBookingOnSeatReservationFailure(any());
    }

    @Test
    void should_mark_failed_when_seat_not_reserved() {
        SeatReservedEvent event =
                new SeatReservedEvent("B1", false,500);

        listener.consumeSeatReserveEvents(event);

        verify(bookingService).handleBookingOnSeatReservationFailure("B1");
        verify(bookingService, never()).markBookingPending(any());
    }
}
