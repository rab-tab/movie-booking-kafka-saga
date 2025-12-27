package com.javatechie.service;

import com.javatechie.config.BookingStatus;
import com.javatechie.entity.Booking;
import com.javatechie.events.BookingCreatedEvent;
import com.javatechie.messaging.BookingEventProducer;
import com.javatechie.repository.BookingRepository;
import com.javatechie.request.BookingRequest;
import com.javatechie.response.BookingResponse;
import com.javatechie.utils.mapper.EntityToBookingResponseMapper;
import com.javatechie.utils.mapper.BookingRequestToEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;

    private final BookingEventProducer bookingEventProducer;

    public BookingService(BookingRepository bookingRepository, BookingEventProducer bookingEventProducer) {
        this.bookingEventProducer = bookingEventProducer;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Reserves seats for a given show and user.
     * Validates the request, maps it to an entity, persists it and returns a response DTO.
     *
     * @param request the seat reservation request
     * @return SeatReserveResponse containing reservation details
     * @throws IllegalArgumentException if the request is invalid
     */

    public BookingResponse bookSeats(BookingRequest request) {

        log.info("Booking seats for user {} for show {}", request.userId(), request.showId());

        // Map request -> entity
        var reservationEntity = BookingRequestToEntityMapper.map(request);

        // Persist and map to response
        var savedReservation = bookingRepository.save(reservationEntity);

        // Publish booking created event
        var bookingCreatedEvent = buildBookingCreateEvents(savedReservation);
        bookingEventProducer.publishBookingEvents(bookingCreatedEvent);

        var response = EntityToBookingResponseMapper.map(savedReservation);

        log.info("Seats confirmed with reservation id {}", response.reservationId());
        return response;
    }

    private BookingCreatedEvent buildBookingCreateEvents(Booking savedReservation) {
        return new BookingCreatedEvent(savedReservation.getBookingCode(), savedReservation.getUserId(), savedReservation.getShowId(), savedReservation.getSeatIds(), savedReservation.getAmount());
    }


    public void handleBookingOnSeatReservationFailure(String bookingId) {
        log.info("BookingService:: Handling booking failure for bookingId {}", bookingId);
        var bookingDetails=bookingRepository.findByBookingCode(bookingId);
        if(bookingDetails!=null){
            bookingDetails.setStatus("FAILED");
            bookingRepository.save(bookingDetails);
            log.info("BookingService:: Booking marked as FAILED for bookingId {}", bookingId);
        }else{
            log.warn("BookingService:: No booking found with bookingId {}", bookingId);
        }

    }
    public void markBookingPending(String bookingId) {
        var booking = bookingRepository.findByBookingCode(bookingId);
        if (booking != null && booking.getStatus() == null) {
            booking.setStatus("PENDING");
            bookingRepository.save(booking);
            log.info("BookingService:: Booking marked as PENDING for bookingId {}", bookingId);
        }
    }

    public void markBookingConfirmed(String bookingId) {

        var booking = bookingRepository.findByBookingCode(bookingId);

        if (booking == null) {
            log.warn("BookingService:: Booking not found for id {}", bookingId);
            return;
        }

        // 🔁 Idempotency
        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            log.info("Booking {} already CONFIRMED. Skipping.", bookingId);
            return;
        }

        // 🚫 Invalid transition
        if (BookingStatus.FAILED.equals(booking.getStatus())) {
            log.warn("Booking {} already FAILED. Cannot confirm.", bookingId);
            return;
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("BookingService:: Booking marked as CONFIRMED for bookingId {}", bookingId);
    }

    public void cancelBooking(String bookingId) {

        var booking = bookingRepository.findByBookingCode(bookingId);

        if (booking == null) {
            log.warn("BookingService:: Booking not found for id {}", bookingId);
            return;
        }

        // 🔁 Idempotency
        if (BookingStatus.FAILED.equals(booking.getStatus())) {
            log.info("Booking {} already FAILED. Skipping.", bookingId);
            return;
        }

        // 🚫 Do not cancel confirmed booking
        if (BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            log.warn("Booking {} already CONFIRMED. Cannot cancel.", bookingId);
            return;
        }

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        log.info("BookingService:: Booking marked as FAILED for bookingId {}", bookingId);
    }

}
