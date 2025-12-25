package com.javatechie.controller;

import com.javatechie.service.SeatInventoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/test/seat-inventory")
public class SeatInventoryTestController {

    private final SeatInventoryService seatInventoryService;

    public SeatInventoryTestController(SeatInventoryService seatInventoryService) {
        this.seatInventoryService = seatInventoryService;
    }

    @PostMapping("/timeout/enable")
    public void enableTimeout() {
        seatInventoryService.enableTimeoutSimulation();
    }

    @PostMapping("/timeout/disable")
    public void disableTimeout() {
        seatInventoryService.disableTimeoutSimulation();
    }
}