package com.example.reservationApi.config;

import com.example.reservationApi.authentication.AuthenticationProviderImpl;
import com.example.reservationApi.authentication.TokenRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Order(1)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthenticationProviderImpl authProvider;
    private final TokenRequestFilter tokenRequestFilter;


    @Autowired
    public WebSecurityConfig(AuthenticationProviderImpl authProvider, TokenRequestFilter tokenRequestFilter) {
        this.authProvider = authProvider;
        this.tokenRequestFilter = tokenRequestFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.POST,"/api/account/**").permitAll()
                .mvcMatchers(HttpMethod.DELETE,"/api/account/**").hasRole("ADMIN")
                .mvcMatchers(HttpMethod.GET, "/api/event/**").authenticated()
                .mvcMatchers("/api/event/**").hasRole("ADMIN")
                .mvcMatchers(HttpMethod.GET, "/api/reservable/**").authenticated()
                .mvcMatchers("/api/reservable/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and().httpBasic().and().csrf().disable().cors();

        http.addFilterBefore(tokenRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
