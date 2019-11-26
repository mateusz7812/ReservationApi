package com.example.reservationApi.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public Account addAccount(@RequestBody Account account, HttpServletResponse response){
        if(!accountService.loginFree(account.getLogin()))
        {
            try {
                response.sendError(400, "login is taken");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        }
        return accountService.save(account);
    }

    @GetMapping("/{id}")
    public Account getAccountWithId(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable UUID id, @RequestBody Account account, HttpServletResponse response){
        if(id.equals(account.getId()))
            return accountService.update(account);
        else{
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable UUID id){
        Account account = accountService.findById(id);
        accountService.delete(account);
    }
}
