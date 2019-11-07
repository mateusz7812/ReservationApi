package com.example.ReservationApi.account;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @JsonProperty("login")
    private String login;

    @NotBlank
    @JsonProperty("password")
    private String password;

    @OneToMany(mappedBy = "account") private List<Event> events;
    //@OneToMany(mappedBy = "account") private List<Reservation> reservations;

    public Account(String login, String password){
        this();
        this.login = login;
        this.password = password;
    }

    public Account(UUID id, String login, String password){
        this(login, password);
        this.id = id;
    }

    Account() {
        super();
        this.events = new ArrayList<>();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<Event> getEvents() {
        return events;
    }

    //public void addEvent(Event event){
    //    events.add(event);
    //}

    public boolean checkPassword(String password){
        return this.password.equals(password);
    }
}
