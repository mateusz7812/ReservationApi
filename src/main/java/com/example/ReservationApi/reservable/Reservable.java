package com.example.ReservationApi.reservable;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="reservable")
public class Reservable {

    @OneToMany(mappedBy = "reservable", fetch=FetchType.EAGER, cascade = CascadeType.REMOVE) private List<Event> event;

    @Id @GeneratedValue private UUID id;

    public Reservable() {
        super();
        //this.event = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public boolean contains(Reservable reservable) {
        return id.equals(reservable.getId());
    }

    //public List<Event> getEvent() {
    //    return event;
    //}
}
