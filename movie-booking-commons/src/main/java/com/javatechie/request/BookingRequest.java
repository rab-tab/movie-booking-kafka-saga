package com.javatechie.request;

import java.util.List;

public record BookingRequest(String reservationId,
                             List<String> seatIds, String userId,
                             long amount) {
}
