package com.example.reservationApi.reservable;

import com.example.reservationApi.reservable.types.Space;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservable")
public class ReservableController {
    private final ReservableService reservableService;

    public ReservableController(ReservableService reservableService) {
        this.reservableService = reservableService;
    }

    @GetMapping
    public List<Reservable> getAllReservableObjects(){
        return reservableService.findAll();
    }

    @GetMapping("/{id}")
    public Reservable getReservableById(@PathVariable UUID id, HttpServletResponse response){
        Reservable reservable = reservableService.findById(id);
        if(reservable != null){
            return reservable;
        }
        else{
            response.setStatus(404);
            return null;
        }
    }

    @PostMapping
    public Reservable addReservable(@RequestBody Reservable reservable) {
        return reservableService.save(reservable);
    }

    @PutMapping("/{id}")
    public Reservable updateReservable(@PathVariable UUID id, @RequestBody HashMap<String, Object> reservableMap, HttpServletResponse response){
        if (reservableMap.containsKey("id")) {
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        } else {
            Reservable reservable = reservableService.findById(id);
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, Object> updatedMap = objectMapper.convertValue(reservable, new TypeReference<>() {
            });
            updatedMap.putAll(reservableMap);
            Reservable updatedReservable = objectMapper.convertValue(updatedMap, Reservable.class);
            return reservableService.update(updatedReservable);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteReservable(@PathVariable UUID id, HttpServletResponse response){
        Reservable reservable = reservableService.findById(id);
        if (reservable.inAnyEvent()) {
            try {
                response.sendError(400, "reservable in event");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return;
        }
        if(reservable instanceof Space)
            if(((Space) reservable).getReservables().size() != 0) {
                try {
                    response.sendError(400, "space cointains reservable");
                } catch (IOException e) {
                    response.setStatus(400);
                }
                return;
            }
        reservableService.delete(reservable);
    }
}
