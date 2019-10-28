package com.example.ReservationApi.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    private final AuthorizationStrategy authorizationStrategy;

    @Autowired
    public AuthorizationService(AuthorizationStrategy authorizationStrategy) {
        this.authorizationStrategy = authorizationStrategy;
    }

    public boolean authorize(AuthorizationPass authorizationPass, String scope){

        return true;
    }
}
