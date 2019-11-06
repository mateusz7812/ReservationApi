package com.example.ReservationApi.reservation;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservable.Reservable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Reservable reservable;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    public Reservation(){
        super();
    }

    public Reservation(UUID id, @NotNull Event event, Reservable reservable){
        super();
        this.id = id;
        this.event = event;
        this.reservable = reservable;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public Account getAccount() {
        return account;
    }

    public Reservable getReservable() {
        return reservable;
    }
}
