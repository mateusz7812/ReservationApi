package com.example.reservationApi.account;

import com.example.reservationApi.admin.Admin;
import com.example.reservationApi.json.IdDeserializer;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, resolver = IdDeserializer.class, property = "id", scope = Account.class)
public class Account {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(unique = true)
    @JsonProperty("login")
    private String login;

    @JsonIgnore
    @OneToOne(mappedBy = "account", cascade = CascadeType.REMOVE)
    private Admin admin;

    @NotBlank
    @JsonProperty("password")
    private String password;

    @JsonIdentityReference(alwaysAsId = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Reservation> reservations;

    public Account(String login, String password){
        this();
        this.login = login;
        this.password = password;
    }

    Account() {
        super();
        admin = null;
        this.reservations = new ArrayList<>();
    }

    @JsonIgnore
    public boolean isAdmin(){
        return admin != null;
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

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
