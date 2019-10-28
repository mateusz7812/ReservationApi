package com.example.ReservationApi.reservation;

import com.example.ReservationApi.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final AuthorizationService authorizationService;

    @Autowired
    public ReservationController(ReservationRepository reservationRepository, AuthorizationService authorizationService) {
        this.reservationRepository = reservationRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Reservation> getReservationsOfEvent(@RequestParam("accountId") UUID accountId, @RequestParam("eventId") UUID eventId){

        return null;
    }

    @PostMapping
    public void addReservation(@RequestBody Reservation reservation){

    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){

    }
}
