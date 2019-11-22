package com.example.reservationApi.observation;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

@Service
public interface ObservationService {
    void addSessionByObservedId(WebSocketSession session, String message);
    void update(UUID id);
}
