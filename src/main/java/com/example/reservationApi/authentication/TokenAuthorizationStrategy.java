package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface TokenAuthorizationStrategy {
    boolean validPassesFormat(String token);

    boolean correctPasses(String token);

    Account getAccount(String token);

    UsernamePasswordAuthenticationToken generateToken(Account account);
}
