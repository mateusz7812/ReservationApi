package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.event.EventRepository;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.ReservableRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

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
        reservationRepository.deleteAll();
        reservableRepository.deleteAll();
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
    public void inheritedEntityRepository(){
        reservableRepository.save(new Seat("A1"));
        Assert.assertEquals(1, reservableRepository.findAll().size());
        Assert.assertEquals(Seat.class, reservableRepository.findAll().get(0).getClass());
    }

    @Test
    public void addSeat() throws JSONException {
        testMethods.addAccount(account, password);
        testMethods.addOneSeat(new Seat("name"));

        Assert.assertEquals(1, reservableRepository.findAll().size());
        Reservable reservable = reservableRepository.findAll().get(0);
        Assert.assertEquals(Seat.class, reservable.getClass());
        Assert.assertEquals("name", ((Seat) reservable).getName());
    }

    @Test
    public void getAllReservableObjects() throws JSONException, JsonProcessingException {
        testMethods.addAccount(account, password);
        testMethods.addOneSeat(new Seat("name"));

        Reservable[] allReservableObjects = testMethods.getAllReservableObjects();

        Seat expected = (Seat) reservableRepository.findAll().get(0);
        Seat actual = (Seat) allReservableObjects[0];
        Assert.assertEquals(1, allReservableObjects.length);
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void addEvent() throws JSONException {
        testMethods.addAccount(account, password);
        Account account = accountRepository.findAll().get(0);
        testMethods.addEvent(new Event(account, "event"));

        Assert.assertEquals(1, eventRepository.findAll().size());
        Event event = eventRepository.findAll().get(0);
        Assert.assertEquals("event", event.getName());
        Assert.assertEquals(account.getId(), event.getAccount().getId());
    }
}
