package com.javatechie.controller;

import com.javatechie.controller.BookingController;
import com.javatechie.response.BookingResponse;
import com.javatechie.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Test
    void should_book_seat() throws Exception {

        BookingResponse response = new BookingResponse("B1", "PENDING");
        when(bookingService.bookSeats(any())).thenReturn(response);

        mockMvc.perform(post("/booking-service/bookSeat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "showId":"S1",
                          "seatIds":["A1"],
                          "userId":"U1",
                          "amount":200
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value("B1"));
    }
}
