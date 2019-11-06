package com.example.ReservationApi.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<Account> allAccounts(){
        return accountRepository.findAll();
    }

    @PostMapping
    public void addAccount(@RequestBody Account account){
        accountRepository.save(account);
    }

    @GetMapping("/{id}")
    public Account getAccountWithId(@PathVariable UUID id){
        return new Account();
    }

    @PutMapping("/{id}")
    public void modifyAccountWithId(@PathVariable UUID id, @RequestBody Account account){

    }


}
