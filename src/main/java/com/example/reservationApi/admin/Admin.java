package com.example.reservationApi.admin;

import com.example.reservationApi.account.Account;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="admins")
public class Admin {
    @GeneratedValue
    @Id
    private UUID id;

    //@MapsId
    //@JoinColumn(name = "id")
    @OneToOne(fetch = FetchType.EAGER)
    @JsonIdentityReference(alwaysAsId = true)
    private Account account;

    public Admin(Account account) {
        super();
        this.account = account;
    }

    public Admin(){}

    public Account getAccount(){
        return account;
    }

    public UUID getId() {
        return id;
    }
}
