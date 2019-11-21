package com.example.reservationApi.reservable;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    public Reservable getReservableById(@PathVariable UUID id){
        return reservableService.findById(id);
    }

    @PostMapping
    public Reservable addReservable(@RequestBody Reservable reservable) {
        return reservableService.save(reservable);
    }

    @PutMapping("/{id}")
    public Reservable updateReservable(@PathVariable UUID id, @RequestBody Reservable reservable, HttpServletResponse response){
        if(id.equals(reservable.getId()))
            return reservableService.update(reservable);
        else{
            response.setStatus(400);
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void deleteReservable(@PathVariable UUID id){
        Reservable reservable = reservableService.findById(id);
        reservableService.delete(reservable);
    }
}
