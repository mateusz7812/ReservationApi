package com.example.reservationApi.observation;

import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReservationObservationService implements ObservationService {

    static private Map<UUID, WebSocketObserver> observers = new HashMap<>();
    static EventService eventService;

    @Autowired
    public ReservationObservationService(EventService eventService){
        ReservationObservationService.eventService = eventService;
    }

    @Override
    public void addSessionByObservedId(WebSocketSession session, String stringEventId) {
            UUID eventId = UUID.fromString(stringEventId);
            if(!observers.containsKey(eventId))
                observers.put(eventId, new WebSocketObserver());
            observers.get(eventId).addSession(session);
    }

    @Override
    public void update(UUID id) {
        Event event = eventService.findById(id);
        if(event == null)
            return;
        if(!observers.containsKey(id))
            return;
        WebSocketObserver webSocketObserver = observers.get(id);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", event.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        webSocketObserver.update(jsonObject.toString());

    }

}
