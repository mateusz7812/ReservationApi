package com.example.reservationApi.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
        if(reservationService.valid(reservation))
            return reservationService.save(reservation);
        else{
            response.setStatus(400);
            return null;
        }
    }

    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable UUID id, @RequestBody Reservation reservation, HttpServletResponse response){
        if(reservationService.valid(reservation)){
            if (id.equals(reservation.getId()))
                return reservationService.update(reservation);
        }
        response.setStatus(400);
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){
        Reservation reservation = reservationService.findById(id);
        reservationService.delete(reservation);
    }
}
