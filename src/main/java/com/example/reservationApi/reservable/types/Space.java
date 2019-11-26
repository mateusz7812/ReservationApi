package com.example.reservationApi.reservable.types;

import com.example.reservationApi.json.ReservableDeserializer;
import com.example.reservationApi.reservable.Reservable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "spaces")
public class Space extends Reservable{
    @JsonDeserialize(using = ReservableDeserializer.class)
    @JsonIdentityReference(alwaysAsId = true)
    @OneToMany(mappedBy = "space", fetch = FetchType.EAGER)
    private List<Reservable> reservables  = new ArrayList<>();

    public Space(){
        this("Space");
    }

    public Space(String name){
        this(name, null);
    }

    public Space(String name, Space space){
        super(name, space);
    }

    @Override
    public boolean contains(Reservable reservable) {
        return getReservables().stream().filter(reservable1 -> reservable1.getId().equals(reservable.getId())).toArray().length != 0;
    }

    public List<Reservable> getReservables(){
        return reservables;
    }

}
