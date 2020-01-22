package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountRepository;
import com.example.reservationApi.authentication.Token.Token;
import com.example.reservationApi.authentication.Token.TokenRepository;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventRepository;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableRepository;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class SocketTests {
    @Value("${local.server.port}")
    private int port;
    private String URL;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ReservableRepository reservableRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    TokenRepository tokenRepository;

    private CompletableFuture<Reservable> completableFuture;


    class TestWebSocketHandler implements WebSocketHandler{
        @Override
        public void afterConnectionEstablished(WebSocketSession webSocketSession) { }

        @Override
        public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
            Object payload = webSocketMessage.getPayload();
            Reservable reservable = mapper.readValue(String.valueOf(payload), Reservable.class);
            completableFuture.complete(reservable);
        }

        @Override
        public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) { }

        @Override
        public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) { }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }


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
        //String plainCredentials="login" + ":" + "password";
        //String base64Credentials = Base64.getEncoder().encodeToString(plainCredentials.getBytes());
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        //headers.add("Authorization", "Basic " + base64Credentials);
        headers.setBasicAuth(account.getLogin(), account.getPassword());
        client.doHandshake(new TestWebSocketHandler(), headers, URI.create(URL + uuid)).get(10, SECONDS);

        Reservation reservation = reservationRepository.save(new Reservation(account, event, reservable));

        Reservable reservableFromWebSocket = completableFuture.get(10, SECONDS);

        Assert.assertEquals(reservable.getId(), reservableFromWebSocket.getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservableFromWebSocket.getId());
    }

    @Test
    public void testCreateConnectionWithToken() throws InterruptedException, ExecutionException, TimeoutException {
        Account account = accountRepository.save(new Account("login", "password"));
        Reservable reservable = reservableRepository.save(new Seat("seat1"));
        Event event = eventRepository.save(new Event(reservable, "event1"));
        String uuid = String.valueOf(event.getId());
        Token token = tokenRepository.save(new Token("token.token.token.tok", account));

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.setBearerAuth(token.getToken());
        client.doHandshake(new TestWebSocketHandler(), headers, URI.create(URL + uuid)).get(10, SECONDS);

        Reservation reservation = reservationRepository.save(new Reservation(account, event, reservable));

        Reservable reservableFromWebSocket = completableFuture.get(10, SECONDS);

        Assert.assertEquals(reservable.getId(), reservableFromWebSocket.getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservableFromWebSocket.getId());
    }

}