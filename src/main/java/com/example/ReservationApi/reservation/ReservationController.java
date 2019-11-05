package com.example.ReservationApi.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @PostMapping
    public void addReservation(@RequestBody Reservation reservation){

    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){

    }
}
