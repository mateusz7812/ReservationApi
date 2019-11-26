package com.example.reservationApi.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @PostMapping
    public Reservation addReservation(@RequestBody Reservation reservation, HttpServletResponse response){
        if (isValid(reservation, response)) return null;
        return reservationService.save(reservation);
    }

    private boolean isValid(@RequestBody Reservation reservation, HttpServletResponse response) {
        if (!reservationService.isCorrect(reservation)) {
            try {
                response.sendError(400, "reservation is invalid");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return true;
        }
        if (reservationService.reservationIsTaken(reservation)){
            try {
                response.sendError(400, "reservable is taken");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return true;
        }
        return false;
    }

    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable UUID id, @RequestBody Reservation reservation, HttpServletResponse response){
        //if (isValid(reservation, response)) return null;

        if (!id.equals(reservation.getId())) {
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
        }
        return reservationService.update(reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){
        Reservation reservation = reservationService.findById(id);
        reservationService.delete(reservation);
    }
}
