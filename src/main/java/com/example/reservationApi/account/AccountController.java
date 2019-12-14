package com.example.reservationApi.account;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
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
    public List<Account> allAccounts(@PathParam("login") String login){
        if (login != null){
            return Collections.singletonList(accountService.findByLogin(login));
        }
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
    public Account getAccountWithId(@PathVariable UUID id, HttpServletResponse response) {
        Account account = accountService.findById(id);
        if(account != null){
            return account;
        }
        else{
            response.setStatus(404);
            return null;
        }
    }

    @PutMapping("/{id}")
    public Account updateAccount(@PathVariable UUID id, @RequestBody HashMap<String, Object> updateMap, HttpServletResponse response, Principal principal){
        Account requester = accountService.findByLogin(principal.getName());
        if (!requester.isAdmin() && !requester.getId().equals(id)){
            try {
                response.sendError(403);
            } catch (IOException e) {
                response.setStatus(403);
            }
            return null;
        }
        if (updateMap.containsKey("id")) {
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        }
        Account account = accountService.findById(id);
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> accountMap = mapper.convertValue(account, new TypeReference<>() {
        });
        accountMap.put("password", account.getPassword());
        accountMap.putAll(updateMap);
        Account updatedAccount = mapper.convertValue(accountMap, Account.class);
        return accountService.update(updatedAccount);

    }

    @DeleteMapping("/{id}")
    public void deleteAccount(@PathVariable UUID id){
        Account account = accountService.findById(id);
        accountService.delete(account);
    }
}
