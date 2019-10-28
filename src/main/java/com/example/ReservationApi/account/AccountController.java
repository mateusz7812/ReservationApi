package com.example.ReservationApi.account;

import com.example.ReservationApi.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private final AuthorizationService authorizationService;

    @Autowired
    public AccountController(AccountRepository accountRepository, AuthorizationService authorizationService) {
        this.accountRepository = accountRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Account> allAccounts(){
        return null;
    }

    @PostMapping
    public void addAccount(@RequestBody Account account){

    }

    @GetMapping("/{id}")
    public Account getAccountWithId(@PathVariable UUID id){
        return new Account();
    }

    @PutMapping("/{id}")
    public void modifyAccountWithId(@PathVariable UUID id, @RequestBody Account account){

    }


}
