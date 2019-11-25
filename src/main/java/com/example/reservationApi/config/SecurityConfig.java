package com.example.reservationApi.config;

import com.example.reservationApi.authentication.PasswordAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final PasswordAuthenticationProvider authProvider;

    @Autowired
    public SecurityConfig(PasswordAuthenticationProvider authProvider) {
        this.authProvider = authProvider;
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
                .mvcMatchers("/api/admin/**").hasRole("ADMIN")
                .mvcMatchers(HttpMethod.GET, "/api/event/**").hasRole("USER")
                .mvcMatchers("/api/event/**").hasRole("ADMIN")
                .mvcMatchers(HttpMethod.GET, "/api/reservable/**").hasRole("USER")
                .mvcMatchers("/api/reservable/**").hasRole("ADMIN")
                .anyRequest().hasRole("USER")
                .and().httpBasic().and().csrf().disable().cors();
    }

}
