package com.example.reservationApi.account;

import org.springframework.beans.factory.annotation.Autowired;
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
        return accountRepository.save(account);
    }

    public boolean loginFree(String login) {
        Object[] sameLoginAccounts = findAll().stream().filter(account -> account.getLogin().equals(login)).toArray();
        return sameLoginAccounts.length == 0;
    }

    public Account findById(UUID id) {
        return accountRepository.findById(id).orElseThrow();
    }

    public boolean checkPassword(String login, String password) {
        List<Account> accounts = accountRepository.findByLoginAndPassword(login, password);
        return !accounts.isEmpty();
    }

    public Account update(Account account){
        return accountRepository.save(account);
    }

    public Account findByLogin(String login){
        return accountRepository.findByLogin(login);
    }

    public void delete(Account account) {
        accountRepository.delete(account);
    }
}
