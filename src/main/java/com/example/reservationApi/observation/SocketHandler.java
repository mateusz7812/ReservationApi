package com.example.reservationApi.observation;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Component
public class SocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        UUID eventId = UUID.fromString(String.valueOf(session.getAttributes().get("eventId")));
        ObservationService.addObserver(eventId, new WebSocketObserver(session));
    }

}
