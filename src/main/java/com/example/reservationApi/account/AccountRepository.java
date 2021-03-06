package com.example.reservationApi.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByLogin(String login);

    List<Account> findByLoginAndPassword(String login, String password);
}
