package com.example.ReservationApi.account;

import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservation.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountRepository accountRepository;
    private ObjectMapper mapper = new ObjectMapper();

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
    public HashMap<String, String> getAccountWithId(@PathVariable UUID id) throws JsonProcessingException {
        Account account = accountRepository.findById(id).orElseThrow();
        HashMap<String, String> accountMap = new HashMap<>();
        accountMap.put("id", account.getId().toString());
        accountMap.put("login", account.getLogin());
        ArrayList<String> eventsList = new ArrayList<>();
        for(Event event: account.getEvents()){
            eventsList.add(event.getId().toString());
        }
        String eventsListString = mapper.writeValueAsString(eventsList);
        accountMap.put("eventsIds", eventsListString);
        ArrayList<String> reservationsIds = new ArrayList<>();
        for(Reservation reservation: account.getReservations()){
            reservationsIds.add(reservation.getId().toString());
        }
        String reservationsListString = mapper.writeValueAsString(reservationsIds);
        accountMap.put("reservationsIds", reservationsListString);
        return accountMap;
    }

    @PutMapping("/{id}")
    public void modifyAccountWithId(@PathVariable UUID id, @RequestBody Account account){

    }


}
