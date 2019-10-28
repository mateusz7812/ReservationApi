package com.example.ReservationApi.reservation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    @NotNull
    private UUID eventID;

    public Reservation(){
        super();
    }

    public Reservation(UUID id, @NotNull UUID eventID){
        super();
        this.id = id;
        this.eventID = eventID;
    }


    public UUID getEventID() {
        return eventID;
    }

    public void setEventID(UUID eventID) {
        this.eventID = eventID;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
