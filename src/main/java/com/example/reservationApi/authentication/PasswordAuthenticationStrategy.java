package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public interface PasswordAuthenticationStrategy {
    boolean valid(Authentication authentication);

    Object getPass(Authentication authentication);

    boolean isPassCorrect(Object pass);

    List<GrantedAuthority> getAuthorities(Account account);

    Account getAccount(Object pass);

    UsernamePasswordAuthenticationToken generateToken(Object pass, List<GrantedAuthority> authorities);
}
