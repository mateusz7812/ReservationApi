package com.example.reservationApi.authentication.Token;

import org.springframework.stereotype.Service;

@Service
public class TokenService {
    final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Token getByToken(String token) {
        return tokenRepository.getByToken(token);
    }

    public Token save(Token token) {
        return tokenRepository.save(token);
    }
}
