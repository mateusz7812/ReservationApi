package com.example.ReservationApi.space;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservation.Reservation;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="places")
public class Place {

    @OneToMany(mappedBy = "place", fetch=FetchType.EAGER)
    private List<Reservation> reservations;

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    public Place(Space space) {
        this.space = space;
    }

    public Space getSpace() {
        return space;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public boolean reservedFor(Event event) {
        for(Reservation reservation: reservations) {
            if (reservation.getEvent().getId() == event.getId()) {
                return true;
            }
        }
        return false;
    }
}
