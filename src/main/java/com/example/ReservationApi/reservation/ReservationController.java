package com.example.ReservationApi.reservation;

import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.ReservableRepository;
import com.example.ReservationApi.reservable.types.Seat;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.json.JSONObject;

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
