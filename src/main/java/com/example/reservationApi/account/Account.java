package com.example.reservationApi.account;

import com.example.reservationApi.authentication.Token.Token;
import com.example.reservationApi.json.IdDeserializer;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.annotation.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY, value = "password")
    private String password;

    @Fetch(FetchMode.SELECT)
    @JsonIdentityReference(alwaysAsId = true)
    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<Reservation> reservations;

    @Fetch(FetchMode.SELECT)
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    private List<String> roles;

    @OneToMany(mappedBy = "account", cascade = CascadeType.REMOVE)
    private List<Token> tokens;

    public Account(String login, String password, List<String> roles){
        this(login, password);
        this.roles = roles;
    }

    public Account(String login, String password){
        this();
        this.login = login;
        this.password = password;
    }

    public Account() {
        super();
        this.roles = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    @JsonIgnore
    public boolean isAdmin(){
        return roles.contains("ROLE_ADMIN");
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

    public String getPassword() {
        return password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        roles.add(role);
    }
}
