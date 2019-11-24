package com.example.reservationApi.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account save(Account account) {
        if(loginFree(account.getLogin()))
            return accountRepository.save(account);
        return null;
    }

    private boolean loginFree(String login) {
        Object[] sameLoginAccounts = findAll().stream().filter(account -> account.getLogin().equals(login)).toArray();
        return sameLoginAccounts.length == 0;
    }

    public Account findById(UUID id) {
        return accountRepository.findById(id).orElseThrow();
    }

    public boolean checkPassword(String name, String password) {
        List<Account> accounts = accountRepository.findAll(Example.of(new Account(name, password)));
        return !accounts.isEmpty();
    }

    public Account update(Account account){
        return accountRepository.save(account);
    }

    public Account getByUsername(String username){
        return accountRepository.findOne(Example.of(new Account(username, null))).orElseThrow();
    }
}