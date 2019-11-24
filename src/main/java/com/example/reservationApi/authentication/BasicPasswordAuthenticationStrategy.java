package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BasicPasswordAuthenticationStrategy implements PasswordAuthenticationStrategy {
    static private AccountService accountService;

    static class Pass{
        String username;
        String password;
        Pass(String username, String password){
            this.username = username;
            this.password = password;
        }
    }

    public BasicPasswordAuthenticationStrategy(AccountService accountService) {
        BasicPasswordAuthenticationStrategy.accountService = accountService;
    }

    @Override
    public boolean valid(Authentication authentication) {
        return (authentication.getCredentials() != null) && (authentication.getName() != null);
    }

    @Override
    public Object getPass(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        return new Pass(username, password);
    }

    @Override
    public boolean isPassCorrect(Object pass) {
            return accountService.checkPassword(((Pass) pass).username, ((Pass) pass).password);
    }

    @Override
    public List<GrantedAuthority> getAuthorities(Account account) {
        return new ArrayList<>(){{
            add(new SimpleGrantedAuthority("ROLE_USER"));}};
    }

    @Override
    public Account getAccount(Object pass) {
        return accountService.getByUsername(((Pass) pass).username);
    }

    @Override
    public UsernamePasswordAuthenticationToken generateToken(Object pass, List<GrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(
                ((Pass) pass).username, ((Pass) pass).password, authorities);
    }
}
