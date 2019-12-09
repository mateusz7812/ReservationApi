package com.example.reservationApi.reservable;

import com.example.reservationApi.event.Event;
import com.example.reservationApi.json.IdDeserializer;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name="reservable")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Space.class, name = "Space"),
        @JsonSubTypes.Type(value = Seat.class, name = "Seat")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, resolver = IdDeserializer.class, property = "id", scope = Reservable.class)
public abstract class Reservable{
    @Id @GeneratedValue protected UUID id;

    private String name;

    @JsonIdentityReference(alwaysAsId = true)
    @Fetch(FetchMode.SELECT)
    @OneToMany(mappedBy = "reservable", fetch=FetchType.EAGER)
    protected List<Event> events = new ArrayList<>();

    @JsonIdentityReference(alwaysAsId = true)
    @Fetch(FetchMode.SELECT)
    @OneToMany(mappedBy = "reservable", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    protected List<Reservation> reservations = new ArrayList<>();

    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    public Reservable(){}

    public Reservable(String name, Space space){
        this.name = name;
        this.space = space;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpace(Space space){
        this.space = space;
    }

    public List<Reservation> getReservations() {
        return  reservations;
    }

    public Space getSpace() {
        return space;
    }

    public List<Event> getEvents() {
        return events;
    }

    public abstract boolean contains(Reservable reservable);

    public boolean takenForEvent(UUID eventId) {
        boolean reservationIsTaken = getReservations().stream().map(Reservation::getEvent).map(Event::getId).filter(eventId1 -> eventId1.equals(eventId)).toArray().length != 0;
        if (!reservationIsTaken){
            if(space != null)
                return space.takenForEvent(eventId);
            else
                return false;
        }
        return true;
    }

    public boolean inAnyEvent() {
        boolean thisInEvent = getEvents().size() != 0;
        boolean spaceInEvent = false;
        if (space != null)
            spaceInEvent = space.inAnyEvent();
        return  thisInEvent || spaceInEvent;
    }
}

