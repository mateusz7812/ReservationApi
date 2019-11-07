package com.example.ReservationApi.event;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public EventController(EventRepository eventRepository, AccountRepository accountRepository) {
        this.eventRepository = eventRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public List<Event> getEvents(){
        return eventRepository.findAll();
    }

    @PostMapping
    public void addEvent(@RequestBody Map<String, String> eventMap){
        UUID accountId = UUID.fromString(eventMap.get("accountId"));
        Account account = accountRepository.findById(accountId).orElseThrow();
        String name = eventMap.get("name");
        Event event = new Event(account, name);
        eventRepository.save(event);
    }

    @GetMapping("/{id}")
    public Event getEventWithId(@PathVariable UUID id){
        return null;
    }

    @PutMapping("/{id}")
    public void editEventWithId(@PathVariable UUID id, @RequestBody Event event){

    }

    @DeleteMapping("/{id}")
    public void deleteEventWithId(@PathVariable UUID id){

    }

}
