package com.example.ReservationApi.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventRepository eventRepository;

    @Autowired
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public List<Event> getEvents(){
        return eventRepository.findAll();
    }

    @PostMapping
    public void addEvent(@RequestBody Event event){
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
