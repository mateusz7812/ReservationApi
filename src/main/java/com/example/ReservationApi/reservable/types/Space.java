package com.example.ReservationApi.reservable.types;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservable.Reservable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "spaces")
public class Space {
    @Id @GeneratedValue private UUID id;
    @NotNull private String name;
    @OneToMany(mappedBy = "space") private List<Event> events;

    public Space(){
        super();
        events = new ArrayList<>();
    }

    public Space(String name){
        this();
        this.name = name;
    }

    public Space(UUID id, String name){
        this(name);
        this.id = id;
    }

    public List<Event> getEvents() {
        return events;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
