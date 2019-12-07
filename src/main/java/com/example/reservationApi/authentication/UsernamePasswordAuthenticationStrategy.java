package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public interface UsernamePasswordAuthenticationStrategy {
    boolean validPassesFormat(Authentication authentication);

    boolean correctPasses(Authentication authentication);

    Account getAccount(Authentication authentication);

    UsernamePasswordAuthenticationToken generateToken(Account account);
}
