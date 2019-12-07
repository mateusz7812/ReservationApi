package com.example.reservationApi.authentication;

import com.example.reservationApi.account.Account;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenRequestFilter extends OncePerRequestFilter {

    private final TokenAuthorizationStrategyImpl authorizationStrategy;

    public TokenRequestFilter(TokenAuthorizationStrategyImpl authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) { chain.doFilter(request, response); return; }

        String tokenStr = requestTokenHeader.substring(7);

        if (!authorizationStrategy.validPassesFormat(tokenStr)){ chain.doFilter(request, response);return; }
        if (!authorizationStrategy.correctPasses(tokenStr)) { chain.doFilter(request, response);return; }

        Account account = authorizationStrategy.getAccount(tokenStr);
        if (account == null){ chain.doFilter(request, response); return; }

        List<GrantedAuthority> roles = account.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account.getLogin(), null, roles);
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        chain.doFilter(request, response);
    }
}