package com.example.ReservationApi.event;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservation.Reservation;
import com.example.ReservationApi.reservable.types.Space;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="events")
public class Event {
    @Id @GeneratedValue private UUID id;

    @NotBlank private String name;

    @ManyToOne @JoinColumn(name = "reservable_id") private Reservable reservable;

    @OneToMany(mappedBy = "event", fetch=FetchType.EAGER, cascade = CascadeType.REMOVE) private List<Reservation> reservations;
    @ManyToOne @JoinColumn(name = "account_id") private Account account;

    public Event(){
        super();
    }


    public Event(Account account, Reservable reservable, String name) {
        this();
        this.account = account;
        this.reservable = reservable;
        this.name = name;
    }

    public Event(UUID id, Account account, Reservable reservable, String name) {
        this(account, reservable, name);
        this.id = id;
    }


    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Reservable getReservable() {
        return reservable;
    }
}
