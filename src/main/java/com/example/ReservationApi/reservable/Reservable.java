package com.example.ReservationApi.reservable;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="reservable")
public class Reservable {

    //@OneToMany(mappedBy = "reservable", fetch=FetchType.EAGER)
    //@JsonIgnore
    //private List<Reservation> reservations;

    @Id
    @GeneratedValue
    private UUID id;


    public Reservable() {
        super();
    }

    //public List<Reservation> getReservations() {
    //    return reservations;
    //}

    /*public boolean reservedFor(Event event) {
        for(Reservation reservation: reservations) {
            if (reservation.getEvent().getId() == event.getId()) {
                return true;
            }
        }
        return false;
    }
*/
    public UUID getId() {
        return id;
    }
}
