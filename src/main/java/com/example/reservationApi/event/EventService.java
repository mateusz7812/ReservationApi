package com.example.reservationApi.event;

import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ReservableService reservableService;

    @Autowired
    public EventService(EventRepository eventRepository, ReservableService reservableService) {
        this.eventRepository = eventRepository;
        this.reservableService = reservableService;
    }


    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    boolean valid(Event event) {
        if (reservableIsInOtherEventThen(event)) return false;
        return true;
    }

    private boolean reservableIsInOtherEventThen(Event event) {
        Reservable reservable = reservableService.findById(event.getReservable().getId());
        List<Event> events = reservable.getEvents();
        List<Event> inCollision = events.stream().filter(event1 -> inCollision(event1, event)).collect(Collectors.toList());
        if (!inCollision.isEmpty())
            return true;
        return false;
    }

    private boolean inCollision(Event event1, Event event2) {
        if(event1.getEndDate() == 0 || event2.getEndDate() == 0)
            return false;
        boolean event2BeginAfterEvent1 = event1.getEndDate() <= event2.getStartDate();
        boolean event1BeginAfterEvent2 = event1.getStartDate() >= event2.getEndDate();
        boolean eventsNotCollides = event2BeginAfterEvent1 || event1BeginAfterEvent2;
        return !eventsNotCollides;
    }

    public Event findById(UUID id) {
        return eventRepository.findById(id).orElse(null);
    }

    public void delete(Event event) {
        eventRepository.delete(event);
    }

    public Event update(Event event) {
        return eventRepository.save(event);
    }
}
