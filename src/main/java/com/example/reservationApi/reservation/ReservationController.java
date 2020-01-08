package com.example.reservationApi.reservation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    @GetMapping
    public List<Reservation> getAllReservations(){
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable UUID id){
        return reservationService.findById(id);
    }

    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable UUID id, @RequestBody HashMap<String, Object> updateMap, HttpServletResponse response){

        if (updateMap.containsKey("id") && !UUID.fromString((String) updateMap.get("id")).equals(id)) {
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        Reservation reservation = reservationService.findById(id);
        HashMap<String, Object> reservationMap = mapper.convertValue(reservation, new TypeReference<>() {});
        reservationMap.putAll(updateMap);
        Reservation updatedReservation = mapper.convertValue(reservationMap, Reservation.class);

        return reservationService.update(updatedReservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){
        Reservation reservation = reservationService.findById(id);
        reservationService.delete(reservation);
    }
}
