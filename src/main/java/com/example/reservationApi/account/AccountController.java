package com.example.reservationApi.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> allAccounts(){
        return accountService.findAll();
    }

    @PostMapping
    public Account addAccount(@RequestBody Account account){
        return accountService.save(account);
    }

    @GetMapping("/{id}")
    public Account getAccountWithId(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @PutMapping("/{id}")
    public Account modifyAccountWithId(@PathVariable UUID id, @RequestBody Account account){
        if(id.equals(account.getId()))
            return accountService.update(account);
        return null;
    }


}
