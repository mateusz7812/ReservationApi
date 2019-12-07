package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BasicUsernamePasswordAuthenticationStrategy implements UsernamePasswordAuthenticationStrategy {
    final private AccountService accountService;

    public BasicUsernamePasswordAuthenticationStrategy(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public boolean validPassesFormat(Authentication authentication) {
        return (authentication.getCredentials() != "") && (authentication.getPrincipal() != "");
    }

    @Override
    public boolean correctPasses(Authentication authentication) {
        Account byLogin = accountService.findByLogin(String.valueOf(authentication.getPrincipal()));
        if (byLogin == null)
            return false;
        String requestPassword = String.valueOf(authentication.getCredentials());
        return byLogin.getPassword().equals(requestPassword);
    }

    @Override
    public Account getAccount(Authentication authentication) {
        return accountService.findByLogin(String.valueOf(authentication.getPrincipal()));
    }

    @Override
    public UsernamePasswordAuthenticationToken generateToken(Account account) {
        List<GrantedAuthority> roles = account.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new UsernamePasswordAuthenticationToken(account.getLogin(), account.getPassword(), roles);
    }
}
