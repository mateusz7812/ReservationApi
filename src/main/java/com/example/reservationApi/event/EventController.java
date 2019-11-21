package com.example.reservationApi.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<Event> getEvents(){
        return eventService.findAll();
    }

    @PostMapping
    public Event addEvent(@RequestBody Event event, HttpServletResponse response){
        if (eventService.valid(event)) {
            return eventService.save(event);
        } else {
            response.setStatus(400);
            return null;
        }

    }

    @GetMapping("/{id}")
    public Event getEventWithId(@PathVariable UUID id){
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    public Event editEventWithId(@PathVariable UUID id, @RequestBody Event event, HttpServletResponse response){
        if(id.equals(event.getId()))
            return eventService.update(event);
        else{
            response.setStatus(400);
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void deleteEventWithId(@PathVariable UUID id){
        Event event = eventService.findById(id);
        eventService.delete(event);
    }

}
