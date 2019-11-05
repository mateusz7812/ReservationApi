package com.example.ReservationApi.security;

import com.example.ReservationApi.account.AccountRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final AccountRepository accountRepository;

    public UsernamePasswordAuthenticationProvider(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        Object[] accounts = accountRepository.findAll().stream().filter(account -> account.getLogin().equals(name) && account.checkPassword(password)).toArray();
        if(accounts.length == 1){
            return new UsernamePasswordAuthenticationToken(
                  name, password, new ArrayList<>());
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
