package com.example.reservationApi.observation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocketHandler extends TextWebSocketHandler {
    static ReservationObservationService reservationObservationService;

    @Autowired
    SocketHandler(ReservationObservationService reservationObservationService){
        SocketHandler.reservationObservationService = reservationObservationService;
    }

    public SocketHandler() {
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        reservationObservationService.addSessionByObservedId(session, String.valueOf(session.getAttributes().get("eventId")));
    }

}
