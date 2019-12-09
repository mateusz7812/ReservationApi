package com.example.reservationApi.reservable.types;

import com.example.reservationApi.reservable.Reservable;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@JsonTypeName("Seat")
@Table(name = "seats")
public class Seat extends Reservable {
    @Override
    public boolean contains(Reservable reservable) {
        return getId().equals(reservable.getId());
    }

    public Seat(){
        this("Seat");
    }

    public Seat(String name) {
        this(name, null);
    }

    public Seat(String name, Space space){
        super(name, space);
    }
}
