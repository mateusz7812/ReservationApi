package com.example.reservationApi.event;

import com.example.reservationApi.json.IdDeserializer;
import com.example.reservationApi.json.ReservableDeserializer;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="events")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, resolver = IdDeserializer.class, property = "id", scope = Event.class)
public class Event{
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    private String name;

    @NonNull
    private long startDate;

    @NonNull
    private long endDate;

    @Fetch(FetchMode.SELECT)
    @JsonDeserialize(using = ReservableDeserializer.class)
    @JsonTypeInfo( use = JsonTypeInfo.Id.NONE)
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservable_id")
    private Reservable reservable;

    @Fetch(FetchMode.SELECT)
    @JsonIdentityReference(alwaysAsId = true)
    @OneToMany(mappedBy = "event", fetch=FetchType.EAGER, cascade = CascadeType.REMOVE)
    private List<Reservation> reservations = new ArrayList<>();

    public Event(){this(null, null);}

    public Event(Reservable reservable, String name) {
        this(reservable, name, 0, 0);
    }

    public Event(Reservable reservable, String name, long startDate, long endDate){
        super();
        setReservable(reservable);
        setName(name);
        setStartDate(startDate);
        setEndDate(endDate);
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

    public Reservable getReservable() {
        return reservable;
    }

    public void setReservable(Reservable reservable) {
        this.reservable = reservable;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(long endDate) {
        if(endDate>=this.startDate)
        this.endDate = endDate;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}

