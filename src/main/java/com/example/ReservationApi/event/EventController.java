package com.example.ReservationApi.event;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    private Map<String, String> getEventMap(Event event) {
        Map<String, String> eventMap = new HashMap<String, String>();
        eventMap.put("id", event.getId().toString());
        eventMap.put("name", event.getName());
        eventMap.put("accountId", event.getAccount().getId().toString());
        return eventMap;
    }

    @GetMapping
    public Map[] getEvents(){
        List<Event> events = eventRepository.findAll();
        List<Map<String, String>> eventsMapsList = new ArrayList<>();
        for(Event event: events){
            Map<String, String> eventMap = getEventMap(event);
            eventsMapsList.add(eventMap);
        }
        return eventsMapsList.toArray(new Map[0]);
    }

    @PostMapping
    public void addEvent(@RequestBody Map<String, String> eventMap){
        UUID accountId = UUID.fromString(eventMap.get("accountId"));
        Account account = accountRepository.findById(accountId).orElseThrow();
        String name = eventMap.get("name");
        Event event = new Event(account, null, name);
        eventRepository.save(event);
    }

    @GetMapping("/{id}")
    public Map<String, String> getEventWithId(@PathVariable UUID id){
        Event event = eventRepository.findById(id).orElseThrow();
        return getEventMap(event);
    }

    @PutMapping("/{id}")
    public void editEventWithId(@PathVariable UUID id, @RequestBody Event event){

    }

    @DeleteMapping("/{id}")
    public void deleteEventWithId(@PathVariable UUID id){

    }

}
