package com.example.ReservationApi.reservable.types;

import com.example.ReservationApi.reservable.Reservable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat extends Reservable {
    private String name;

    public Seat(){
        super();
    }

    public Seat(String name) {
        this();
        this.name = name;
    }


}
