package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.event.EventRepository;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.ReservableRepository;
import com.example.ReservationApi.reservation.Reservation;
import com.example.ReservationApi.reservation.ReservationRepository;
import com.example.ReservationApi.reservable.types.Seat;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.function.Function;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    private ReservableRepository reservableRepository;

    private Account account = new Account("user", "password");
    private String password = "password";

    private TestMethods testMethods;


    @Before
    public void before(){
        testMethods = new TestMethods(account.getLogin(), password, testRestTemplate);
        reservableRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void authorization(){
        ResponseEntity<String> notAuthorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(401, notAuthorizedGetAccountsResponse.getStatusCodeValue());

        Account account = new Account("user", "password");
        accountRepository.save(account);

        ResponseEntity<String> authorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(200, authorizedGetAccountsResponse.getStatusCodeValue());

    }

    @Test
    public void addAccount() throws JSONException {
        testMethods.addAccount(account, password);

        List<Account> accounts = accountRepository.findAll();
        Assert.assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        Assert.assertEquals("user", account.getLogin());
        Assert.assertTrue(account.checkPassword("password"));
    }

    @Test
    public void getAccount() throws JSONException, JsonProcessingException {
        accountRepository.save(account);
        account = accountRepository.findAll().get(0);
        eventRepository.save(new Event(account, null, "event1"));
        Event event = eventRepository.findAll().get(0);

        Account actual = testMethods.getAccount(account.getId());

        Assert.assertEquals(account.getId(), actual.getId());
        Assert.assertEquals(account.getLogin(), actual.getLogin());
        Assert.assertEquals(1, actual.getEvents().size());
        Assert.assertEquals(event.getId(), actual.getEvents().get(0).getId());
    }

    @Test
    public void inheritedEntityRepository(){
        reservableRepository.save(new Seat("A1"));
        Assert.assertEquals(1, reservableRepository.findAll().size());
        Assert.assertEquals(Seat.class, reservableRepository.findAll().get(0).getClass());
    }

    @Test
    public void addSeat() throws JSONException {
        accountRepository.save(account);//to authorize

        testMethods.addOneSeat(new Seat("name"));

        Assert.assertEquals(1, reservableRepository.findAll().size());
        Reservable reservable = reservableRepository.findAll().get(0);
        Assert.assertEquals(Seat.class, reservable.getClass());
        Assert.assertEquals("name", ((Seat) reservable).getName());
    }

    @Test
    public void getAllReservableObjects() throws JSONException, JsonProcessingException {
        accountRepository.save(account);
        reservableRepository.save(new Seat("name"));

        Reservable[] allReservableObjects = testMethods.getAllReservableObjects();

        Seat expected = (Seat) reservableRepository.findAll().get(0);
        Seat actual = (Seat) allReservableObjects[0];
        Assert.assertEquals(1, allReservableObjects.length);
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void addEvent() throws JSONException {
        accountRepository.save(account);
        Account account = accountRepository.findAll().get(0);
        testMethods.addEvent(new Event(account, null, "event"));

        Assert.assertEquals(1, eventRepository.findAll().size());
        Event event = eventRepository.findAll().get(0);
        Assert.assertEquals("event", event.getName());
        Assert.assertEquals(account.getId(), event.getAccount().getId());
    }

    @Test
    public void getAllEvents() throws JSONException, JsonProcessingException {
        accountRepository.save(account);
        Account account = accountRepository.findAll().get(0);
        eventRepository.save(new Event(account, null, "event"));

        Event[] events = testMethods.getAllEvents();

        Assert.assertEquals(1, events.length);
        Event event = events[0];
        Assert.assertEquals(account.getId(), event.getAccount().getId());
        Assert.assertEquals("event", event.getName());
    }

    @Test
    public void getEventWithId() throws JsonProcessingException {
        accountRepository.save(account);
        Account account = accountRepository.findAll().get(0);
        eventRepository.save(new Event(account, null, "event"));
        Event expected = eventRepository.findAll().get(0);

        Event actual = testMethods.getEventWithId(expected.getId().toString());

        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getAccount().getId(), actual.getAccount().getId());
    }

    @Test
    public void addReservation() throws JSONException {
        accountRepository.save(account);
        Account account = accountRepository.findAll().get(0);
        reservableRepository.save(new Seat("name"));
        Reservable reservable = reservableRepository.findAll().get(0);
        eventRepository.save(new Event(account, reservable, "event"));
        Event event = eventRepository.findAll().get(0);

        testMethods.addReservation(account, event, reservable);

        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservation = reservations.get(0);
        Assert.assertEquals(account.getId(), reservation.getAccount().getId());
        Assert.assertEquals(event.getId(), reservation.getEvent().getId());
        Assert.assertEquals(reservable.getId(), reservation.getReservable().getId());
    }

    @Test
    public void addBadReservation() throws JSONException {
        accountRepository.save(account);
        Account account = accountRepository.findAll().get(0);

        reservableRepository.save(new Seat("name1"));
        Reservable reservable1 = reservableRepository.findAll().get(0);

        reservableRepository.save(new Seat("name2"));
        Seat reservable2 = reservableRepository.findOne(Example.of(new Seat("name2"))).orElseThrow();

        eventRepository.save(new Event(account, reservable1, "event1"));

        eventRepository.save(new Event(account, reservable2 , "event2"));
        Event event2 = eventRepository.findOne(Example.of(new Event(null, null, "event2"))).orElseThrow();

        testMethods.addReservation(account, event2, reservable1);

        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(0, reservations.size());
    }
}
