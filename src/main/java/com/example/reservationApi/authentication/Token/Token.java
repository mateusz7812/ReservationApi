package com.example.reservationApi.authentication.Token;

import com.example.reservationApi.account.Account;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
public class Token {
    @Id
    @GeneratedValue
    UUID id;

    @NotBlank
    String token;

    @NotNull
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(fetch = FetchType.EAGER)
    Account account;

    public Token(){}

    public Token(String token, Account account){
        this.token = token;
        this.account = account;
    }

    public UUID getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Account getAccount() {
        return account;
    }
}
