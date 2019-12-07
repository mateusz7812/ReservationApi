package com.example.reservationApi.event;

import com.example.reservationApi.json.ConfiguredMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
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
        if (eventService.reservableIsInOtherEventThen(event)) {
            try {
                response.sendError(400, "reservable is in other event then");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        }
        return eventService.save(event);
    }

    @GetMapping("/{id}")
    public Event getEventWithId(@PathVariable UUID id){
        return eventService.findById(id);
    }

    @PutMapping("/{id}")
    public Event editEventWithId(@PathVariable UUID id, @RequestBody HashMap<String, Object> updateMap, HttpServletResponse response){

        if (updateMap.containsKey("id")) {
            try {
                response.sendError(400, "id is unchangable");
            } catch (IOException e) {
                response.setStatus(400);
            }
            return null;
        }

        ObjectMapper mapper = new ConfiguredMapper();

        Event event = eventService.findById(id);
        HashMap<String, Object> eventMap = mapper.convertValue(event, new TypeReference<>() {});
        eventMap.putAll(updateMap);
        Event updatedEvent = mapper.convertValue(eventMap, Event.class);

        return eventService.update(updatedEvent);

    }

    @DeleteMapping("/{id}")
    public void deleteEventWithId(@PathVariable UUID id){
        Event event = eventService.findById(id);
        eventService.delete(event);
    }

}
