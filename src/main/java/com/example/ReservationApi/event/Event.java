package com.example.ReservationApi.event;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.reservation.Reservation;
import com.example.ReservationApi.space.Place;
import com.example.ReservationApi.space.Space;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="events")
public class Event {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String name;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @OneToMany(mappedBy = "event")
    private List<Reservation> reservations;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Event(){
        super();
    }

    public Event(UUID id, Account account, Space space, String name){
        super();

        this.id = id;
        this.account = account;
        this.space = space;
        this.name = name;
        this.reservations = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Space getSpace() {
        return space;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public Place[] getFreeSeats() {
        return space.getFreePlaces(this);
    }
}
