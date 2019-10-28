package com.example.ReservationApi.event;

import com.example.ReservationApi.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventRepository eventRepository;
    private final AuthorizationService authorizationService;

    @Autowired
    public EventController(EventRepository eventRepository, AuthorizationService authorizationService) {
        this.eventRepository = eventRepository;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public List<Event> getEvents(){
        return null;
    }

    @PostMapping
    public void addEvent(@RequestBody Event event){

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
