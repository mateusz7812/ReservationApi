package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
import com.example.reservationApi.authentication.Token.Token;
import com.example.reservationApi.authentication.Token.TokenService;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventService;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableService;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JsonConvertTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AccountService accountService;

    @Autowired
    EventService eventService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    private ReservableService reservableService;

    @Autowired
    private TokenService tokenService;

    @Test
    public void tokenJsonConverter() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Token token = tokenService.save(new Token("tokentokentoken", account));
        String tokenAsString = objectMapper.writeValueAsString(token);

        Token readedToken = objectMapper.readValue(tokenAsString, Token.class);
        Assert.assertEquals(token.getToken(), readedToken.getToken());
        Assert.assertEquals(token.getAccount().getId(), readedToken.getAccount().getId());
    }

    @Test
    public void accountJsonConverter() throws JsonProcessingException, JSONException {
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account = accountService.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable));
        account = accountService.findById(account.getId());

        String accountJsonString = objectMapper.writeValueAsString(account);

        JSONObject jsonObject = new JSONObject(accountJsonString);
        Assert.assertFalse(jsonObject.has("password"));
        jsonObject.put("password", "password");

        Account accountFromJson = objectMapper.readValue(jsonObject.toString(), Account.class);

        Assert.assertEquals(account.getId(), accountFromJson.getId());
        Assert.assertEquals(account.getLogin(), accountFromJson.getLogin());
        Assert.assertEquals(account.getPassword(), accountFromJson.getPassword());
        Assert.assertNotNull(account.getReservations());
        Assert.assertEquals(1, accountFromJson.getReservations().size());
        Assert.assertEquals(reservation.getId(), accountFromJson.getReservations().get(0).getId());
    }

    @Test
    public void eventJsonConvert() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        Seat seat1 = new Seat("seat1");
        reservableService.save(seat1);
        seat1 = (Seat) reservableService.findAll().get(0);
        Event event = new Event(seat1, "event1", 0, 0);

        String eventJsonString = objectMapper.writeValueAsString(event);
        Event eventFromJson = objectMapper.readValue(eventJsonString, Event.class);

        Assert.assertEquals(event.getId(), eventFromJson.getId());
        Assert.assertEquals(event.getName(), eventFromJson.getName());
        Assert.assertNotNull(eventFromJson.getReservable());
        Assert.assertEquals(event.getReservable().getId(), eventFromJson.getReservable().getId());
    }

    @Test
    public void reservationJsonConvert() throws JsonProcessingException {
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account1 = accountService.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationService.save(new Reservation(account1, event, reservable));

        String reservationJsonString = objectMapper.writeValueAsString(reservation);
        Reservation reservationFromJson = objectMapper.readValue(reservationJsonString, Reservation.class);

        Assert.assertEquals(reservation.getId(), reservationFromJson.getId());
        Assert.assertEquals(reservation.getAccount().getId(), reservationFromJson.getAccount().getId());
        Assert.assertEquals(reservation.getEvent().getId(), reservationFromJson.getEvent().getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservationFromJson.getReservable().getId());

    }

    @Test
    public void SeatJsonConverter() throws JsonProcessingException {
        Space space = (Space) reservableService.save(new Space("space1"));
        Reservable reservable = reservableService.save(new Seat("seat1", space));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account1 = accountService.save(new Account("reservationTest", "password"));
        reservationService.save(new Reservation(account1, event, reservable));
        reservable = reservableService.findById(reservable.getId());

        String reservableJsonString = objectMapper.writeValueAsString(reservable);
        Reservable reservableFromJson = objectMapper.readValue(reservableJsonString, Reservable.class);

        Assert.assertEquals(reservable.getId(), reservableFromJson.getId());
        Assert.assertEquals(reservable.getName(), reservableFromJson.getName());
        Assert.assertNotNull(reservableFromJson.getEvents());
        Assert.assertEquals(reservable.getEvents().size(), reservableFromJson.getEvents().size());
        Assert.assertEquals(reservable.getEvents().get(0).getId(), reservableFromJson.getEvents().get(0).getId());
        Assert.assertNotNull(reservableFromJson.getReservations());
        Assert.assertEquals(reservable.getReservations().size(), reservableFromJson.getReservations().size());
        Assert.assertEquals(reservable.getReservations().get(0).getId(), reservableFromJson.getReservations().get(0).getId());
    }

    @Test
    public void SpaceJsonConverter() throws JsonProcessingException {
        Space space = (Space) reservableService.save(new Space("space1"));
        reservableService.save(new Seat("seat1", space));
        space = (Space) reservableService.findById(space.getId());

        String spaceJsonString = objectMapper.writeValueAsString(space);
        Space spaceFromJson = (Space) objectMapper.readValue(spaceJsonString, Reservable.class);

        Assert.assertEquals(space.getId(), spaceFromJson.getId());
        Assert.assertNotNull(spaceFromJson.getReservables());
        List<Reservable> spaceReservableList = space.getReservables();
        List<Reservable> spaceFromJsonReservableList = spaceFromJson.getReservables();
        Assert.assertEquals(spaceReservableList.size(), spaceFromJsonReservableList.size());
        Assert.assertEquals(spaceReservableList.get(0).getId(), spaceFromJsonReservableList.get(0).getId());
    }

}
