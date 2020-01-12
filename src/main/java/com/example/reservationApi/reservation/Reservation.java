package com.example.reservationApi.reservation;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.json.IdDeserializer;
import com.example.reservationApi.json.ReservableDeserializer;
import com.example.reservationApi.observation.ObservationService;
import com.example.reservationApi.reservable.Reservable;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, resolver = IdDeserializer.class, property = "id", scope = Reservation.class)
public class Reservation {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @JsonDeserialize(using = ReservableDeserializer.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne
    @JoinColumn(name = "reservable_id")
    private Reservable reservable;

    public Reservation() {
        super();
    }

    public Reservation(Account account, Event event, Reservable reservable) {
        super();
        this.event = event;
        this.reservable = reservable;
        this.account = account;
    }

    @PostRemove
    @PostPersist
    @PostUpdate
    private void updateObservers() {
        ObjectMapper mapper = new ObjectMapper();
        UUID eventId = this.getEvent().getId();
        String data = null;
        try {
            data = mapper.writeValueAsString(this.reservable);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ObservationService.updateAllObserversByObserverdId(eventId, data);
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Reservable getReservable() {
        return reservable;
    }

    public Event getEvent() {
        return event;
    }

    public void setReservable(Reservable reservable) {
        this.reservable = reservable;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

}
