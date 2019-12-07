package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.authentication.Token.Token;
import com.example.reservationApi.authentication.Token.TokenService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenAuthorizationStrategyImpl implements TokenAuthorizationStrategy {
    final private TokenService tokenService;

    public TokenAuthorizationStrategyImpl(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean validPassesFormat(String token) {
        return token.length() == 21;
    }

    @Override
    public boolean correctPasses(String token) {
        Token tokenObject = tokenService.getByToken(token);
        return tokenObject != null;
    }

    @Override
    public Account getAccount(String token) {
        Token tokenObject = tokenService.getByToken(token);
        return tokenObject.getAccount();
    }

    @Override
    public UsernamePasswordAuthenticationToken generateToken(Account account) {
        List<GrantedAuthority> roles = account.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(account.getLogin(), account.getPassword(), roles);
    }
}
