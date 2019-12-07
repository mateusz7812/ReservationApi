package com.example.reservationApi.authentication.Token;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.security.SecureRandom;

@RestController
@RequestMapping("/authenticate")
public class TokenController {
    private final AccountRepository accountService;
    private final TokenRepository tokenRepository;

    public TokenController(AccountRepository accountService, TokenRepository tokenRepository) {
        this.accountService = accountService;
        this.tokenRepository = tokenRepository;
    }

    @GetMapping
    public Token generateToken(Principal principal){
        Account requester = accountService.findByLogin(principal.getName());
        String token = generateRandomString(21);
        while(tokenRepository.getByToken(token) != null)
            token = generateRandomString(21);

        return tokenRepository.save(new Token(token, requester));
    }

    public static String generateRandomString(int length) {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }
}
