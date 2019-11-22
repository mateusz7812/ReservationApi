package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountRepository;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventRepository;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableRepository;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SocketTests {
    @Value("${local.server.port}")
    private int port;
    private String URL;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ReservableRepository reservableRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ReservationRepository reservationRepository;

    private CompletableFuture<JSONObject> completableFuture;

    @Before
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/observe/";
    }

    @Test
    public void testCreateConnection() throws InterruptedException, ExecutionException, TimeoutException {
        Account account = accountRepository.save(new Account("login", "password"));
        Reservable reservable = reservableRepository.save(new Seat("seat1"));
        Event event = eventRepository.save(new Event(reservable, "event1"));
        String uuid = String.valueOf(event.getId());

        StandardWebSocketClient client = new StandardWebSocketClient();
        client.doHandshake(new TestWebSocketHandler(), new WebSocketHttpHeaders(), URI.create(URL + uuid)).get(10, SECONDS);

        reservationRepository.save(new Reservation(account, event, reservable));

        JSONObject data = completableFuture.get(10, SECONDS);

        try {
            Assert.assertEquals("event1", data.get("name"));
        } catch (JSONException e) {
            Assert.fail();
        }
    }

    class TestWebSocketHandler implements WebSocketHandler{
        @Override
        public void afterConnectionEstablished(WebSocketSession webSocketSession) {

        }

        @Override
        public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
            Object payload = webSocketMessage.getPayload();
            completableFuture.complete(new JSONObject(String.valueOf(payload)));
        }

        @Override
        public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) {

        }

        @Override
        public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {

        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}