package com.javatechie.controller;


import com.javatechie.service.SeatInventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatInventoryTestController.class)
class SeatInventoryTestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SeatInventoryService service;

    @Test
    void should_enable_timeout_simulation() throws Exception {
        mockMvc.perform(post("/internal/test/seat-inventory/timeout/enable"))
                .andExpect(status().isOk());

        verify(service).enableTimeoutSimulation();
    }

    @Test
    void should_disable_timeout_simulation() throws Exception {
        mockMvc.perform(post("/internal/test/seat-inventory/timeout/disable"))
                .andExpect(status().isOk());

        verify(service).disableTimeoutSimulation();
    }
}

