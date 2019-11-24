package com.example.reservationApi.observation;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class WebSocketObserver implements Observer{
    private WebSocketSession session;

    public WebSocketObserver(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void update(String data) {
        try {
            session.sendMessage(new TextMessage(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
