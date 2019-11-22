package com.example.reservationApi.observation;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketObserver implements Observer{
    private List<WebSocketSession> sessions = new ArrayList<>();

    public void addSession(WebSocketSession session){
        sessions.add(session);
    }

    @Override
    public void update(String data) {
        try {
            for(WebSocketSession session: sessions)
            {
                session.sendMessage(new TextMessage(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
