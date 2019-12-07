package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProviderImpl implements AuthenticationProvider {
    final BasicUsernamePasswordAuthenticationStrategy authenticationStrategy;

    public AuthenticationProviderImpl(BasicUsernamePasswordAuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        boolean validFormat = authenticationStrategy.validPassesFormat(authentication);

        if(!validFormat)
            return null;

        boolean correctPasses = authenticationStrategy.correctPasses(authentication);
        if (!correctPasses)
            return null;

        Account account = authenticationStrategy.getAccount(authentication);
        return authenticationStrategy.generateToken(account);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
