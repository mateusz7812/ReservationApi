package com.example.ReservationApi.reservation;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.event.EventRepository;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.ReservableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final ReservableRepository reservableRepository;
    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public ReservationController(ReservationRepository reservationRepository, ReservableRepository reservableRepository, EventRepository eventRepository, AccountRepository accountRepository) {
        this.reservationRepository = reservationRepository;
        this.reservableRepository = reservableRepository;
        this.eventRepository = eventRepository;
        this.accountRepository = accountRepository;
    }

    @PostMapping
    public void addReservation(@RequestBody Map<String, String> reservationMap, HttpServletResponse response) throws Exception {
        UUID eventId = UUID.fromString(reservationMap.get("eventId"));
        Event event = eventRepository.findById(eventId).orElseThrow();
        UUID reservableId = UUID.fromString(reservationMap.get("reservableId"));
        Reservable reservable = reservableRepository.findById(reservableId).orElseThrow();
        UUID accountId = UUID.fromString(reservationMap.get("accountId"));
        Account account = accountRepository.findById(accountId).orElseThrow();

        if(event.getReservable().contains(reservable)){
            Reservation reservation = new Reservation(event, reservable, account);
            reservationRepository.save(reservation);
        }

        else{
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST  );
        }
    }

    @DeleteMapping("/{id}")
    public void deleteReservationOfId(@PathVariable UUID id){

    }
}
