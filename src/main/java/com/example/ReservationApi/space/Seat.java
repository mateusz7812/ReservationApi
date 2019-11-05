package com.example.ReservationApi.space;

import com.example.ReservationApi.reservation.Reservation;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "seats")
public class Seat extends Place{
    public Seat(){
        super(null);
    }

    public Seat(Space space) {
        super(space);
    }


}
