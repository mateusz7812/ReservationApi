package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordAuthenticationProvider implements AuthenticationProvider {
    final
    PasswordAuthenticationStrategy passwordAuthenticationStrategy;

    @Autowired
    public PasswordAuthenticationProvider(BasicPasswordAuthenticationStrategy authenticationStrategy) {
        this.passwordAuthenticationStrategy = authenticationStrategy;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(!passwordAuthenticationStrategy.valid(authentication))
            return null;

        Object pass = passwordAuthenticationStrategy.getPass(authentication);
        if(!passwordAuthenticationStrategy.isPassCorrect(pass))
            return null;

        Account account = passwordAuthenticationStrategy.getAccount(pass);
        List<GrantedAuthority> authorities = passwordAuthenticationStrategy.getAuthorities(account);

        return passwordAuthenticationStrategy.generateToken(pass, authorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
