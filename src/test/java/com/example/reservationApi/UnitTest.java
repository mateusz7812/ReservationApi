package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
import com.example.reservationApi.admin.Admin;
import com.example.reservationApi.admin.AdminService;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventService;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableService;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UnitTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    AccountService accountService;

    @Autowired
    AdminService adminService;

    @Autowired
    EventService eventService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    private ReservableService reservableService;

    private TestMethods testMethods;


    @Before
    public void before(){
        testMethods = new TestMethods(testRestTemplate);
        /*reservableService.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        adminRepository.deleteAll();
        accountRepository.deleteAll();*/
    }

    @Test
    public void authorization(){
        ResponseEntity<String> notAuthorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(401, notAuthorizedGetAccountsResponse.getStatusCodeValue());

        Account account = new Account("user", "password");
        accountService.save(account);

        ResponseEntity<String> authorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(200, authorizedGetAccountsResponse.getStatusCodeValue());

    }

    @Test
    public void addAccount() throws JsonProcessingException {
        ResponseEntity<String> response = testMethods.addAccount(new Account("user", "password"));

        Account addedAccount = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        List<Account> accounts = accountService.findAll();
        Assert.assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        Assert.assertEquals("user", account.getLogin());
        Assert.assertEquals(account.getLogin(), addedAccount.getLogin());
        Assert.assertEquals(account.getId(), addedAccount.getId());
    }

    @Test
    public void getAccount() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAccount(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals(account.getId(), actual.getId());
        Assert.assertEquals(account.getLogin(), actual.getLogin());
    }

    @Test
    public void addAccountsWithSameLogin() throws JsonProcessingException, JSONException {
        testMethods.addAccount(new Account("login", "password"));
        Account accountAdded = accountService.findAll().get(0);

        ResponseEntity<String> response2 = testMethods.addAccount(new Account("login", "other"));

        Assert.assertEquals(400, response2.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response2.getBody());
        Assert.assertEquals("login is taken", jsonObject.get("message"));
        List<Account> all = accountService.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(accountAdded.getId(), all.get(0).getId());
    }

    @Test
    public void getAllAccounts() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Account account2 = accountService.save(new Account("login2", "password"));

        ResponseEntity<String> response = testMethods.setPass("login2", "password").getAllAccounts();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Account> accounts = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        List<UUID> uuidList = accounts.stream().map(Account::getId).collect(Collectors.toList());
        Assert.assertTrue(uuidList.contains(account.getId()));
        Assert.assertTrue(uuidList.contains(account2.getId()));
    }

    @Test
    public void editAccount() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        account.setLogin("newLogin");

        ResponseEntity<String> response = testMethods.setPass("user", "password").editAccount(account.getId(), account);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account updatedAccount = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals(account.getLogin(), updatedAccount.getLogin());
    }

    @Test
    public void editAccountByOtherAccount() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        accountService.save(new Account("user2", "password2"));
        account.setLogin("newLogin");

        ResponseEntity<String> response = testMethods.setPass("user2", "password2").editAccount(account.getId(), account);

        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertNotEquals("newLogin", accountService.findById(account.getId()).getLogin());
    }

    @Test
    public void editAccountByAdmin() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Account other = accountService.save(new Account("user2", "password2"));
        adminService.save(new Admin(other));
        account.setLogin("newLogin");

        ResponseEntity<String> response = testMethods.setPass("user2", "password2").editAccount(account.getId(), account);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals("newLogin", accountService.findById(account.getId()).getLogin());
    }

    @Test
    public void editIdAccount() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        UUID oldId = account.getId();
        UUID newId = UUID.randomUUID();
        JSONObject account1Json = new JSONObject(objectMapper.writeValueAsString(account));
        account1Json.put("id", newId);
        account = objectMapper.readValue(account1Json.toString(), Account.class);
        ResponseEntity<String> response = testMethods.setPass("user", "password").editAccount(oldId, account);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
        List<Account> all = accountService.findAll();
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(oldId ,all.get(0).getId());
    }

    @Test
    public void deleteAccount(){
        Account account = accountService.save(new Account("user", "password"));
        adminService.save((new Admin(account)));
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "Event1"));
        reservationService.save(new Reservation(account, event, reservable));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteAccount(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, accountService.findAll().size());
        Assert.assertEquals(0, adminService.findAll().size());
        Assert.assertEquals(0, reservationService.findAll().size());
        Assert.assertEquals(1, reservableService.findAll().size());
        Assert.assertEquals(1, eventService.findAll().size());
    }

    @Test
    public void deleteAccountWrong(){
        Account account = accountService.save(new Account("user", "password"));
        Assert.assertEquals(1, accountService.findAll().size());

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteAccount(account.getId());

        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(1, accountService.findAll().size());
    }

    @Test
    public void addAdmin() throws JsonProcessingException {
        Account adminAccount = accountService.save(new Account("admin", "password"));
        adminService.save(new Admin(adminAccount));
        Account account = accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("admin", "password").addAdmin(new Admin(account));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Admin secondAdmin = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Admin.class);
        Assert.assertEquals(account.getId(), secondAdmin.getAccount().getId());
    }

    @Test
    public void addAdminWrong() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addAdmin(new Admin(account));

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addSpace() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = new Space("space");

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservable(space);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Space addedSpace = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Space.class);
        Assert.assertEquals(space.getName(), addedSpace.getName());
    }

    @Test
    public void addSpaceWithoutAdmin() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        Space space = new Space("space");

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservable(space);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addSeatWithSpace() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = (Space) reservableService.save(new Space("space"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservable(new Seat("name", space));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Seat seat = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Seat.class);
        Assert.assertEquals("name", seat.getName());
        Assert.assertEquals(seat.getSpace().getId(), space.getId());
    }

    @Test
    public void getAllReservableObjects() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        reservableService.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAllReservableObjects();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Reservable> allReservableObjects = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        Seat expected = (Seat) reservableService.findAll().get(0);
        Assert.assertEquals(1, allReservableObjects.size());
        Seat actual = (Seat) allReservableObjects.get(0);
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void getReservableById() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        Seat seat = (Seat) reservableService.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getReservableById(seat.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservable reservable = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservable.class);
        Assert.assertEquals("name", reservable.getName());
    }

    @Test
    public void editReservable() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Seat seat = (Seat) reservableService.save(new Seat("name"));
        seat.setName("seat");

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservable(seat.getId(), seat);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservable reservable = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservable.class);
        Assert.assertEquals("seat", reservable.getName());
    }

    @Test
    public void editIdReservable() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Seat seat = (Seat) reservableService.save(new Seat("name"));
        UUID oldId = seat.getId();
        UUID newId = UUID.randomUUID();
        JSONObject seatJson = new JSONObject(objectMapper.writeValueAsString(seat));
        seatJson.put("id", newId);
        seat = objectMapper.readValue(seatJson.toString(), Seat.class);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservable(oldId, seat);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
    }

    @Test
    public void deleteReservable(){
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = (Space) reservableService.save(new Space("space"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservable(space.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, reservableService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
    }

    @Test
    public void deleteSpace() throws JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = (Space) reservableService.save(new Space("space"));
        reservableService.save(new Seat("seat1", space));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservable(space.getId());

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("space cointains reservable", jsonObject.get("message"));
        List<Reservable> all = reservableService.findAll();
        Assert.assertEquals(2, all.size());
        Assert.assertEquals(1, accountService.findAll().size());
    }

    @Test
    public void deleteSpaceInEvent() throws JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = (Space) reservableService.save(new Space("space"));
        eventService.save(new Event(space, "event1"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservable(space.getId());

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("reservable in event", jsonObject.get("message"));
        Assert.assertEquals(1, reservableService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(1, eventService.findAll().size());
    }

    @Test
    public void deleteSeatInSpaceInEvent() throws JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Space space = (Space) reservableService.save(new Space("space"));
        Seat seat = (Seat) reservableService.save(new Seat("seat1", space));
        eventService.save(new Event(space, "event1"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservable(seat.getId());

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("reservable in event", jsonObject.get("message"));
        Assert.assertEquals(2, reservableService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(1, eventService.findAll().size());
    }

    @Test
    public void addEvent() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addEvent(new Event(reservable, "event"));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event addedEvent = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        List<Event> allEvents = eventService.findAll();
        Assert.assertEquals(1, allEvents.size());
        Event event = allEvents.get(0);
        Assert.assertEquals(event.getId(), addedEvent.getId());
        Assert.assertEquals("event", event.getName());
        Assert.assertNotEquals(null, event.getReservable());
        Assert.assertEquals(reservable.getId(), event.getReservable().getId());
    }

    @Test
    public void addEventWithoutAdmin() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        Reservable reservable = reservableService.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addEvent(new Event(reservable, "event"));

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addTwoEventsOnSameReservbleSameTime() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));

        ResponseEntity<String> response200 = testMethods.setPass("user", "password").addEvent(new Event(reservable, "event1", 200, 300));
        ResponseEntity<String> response400 = testMethods.addEvent(new Event(reservable, "event2", 250, 350));

        Assert.assertEquals(200, response200.getStatusCodeValue());
        Assert.assertEquals(400, response400.getStatusCodeValue());
        JSONObject jsonObjectForResponse400 = new JSONObject(response400.getBody());
        Assert.assertEquals("reservable is in other event then", jsonObjectForResponse400.get("message"));
        List<Event> allEvents = eventService.findAll();
        Assert.assertEquals(1, allEvents.size());
        Event eventFromRepository = allEvents.get(0);
        Event eventFromResponse = objectMapper.readValue(Objects.requireNonNull(response200.getBody()), Event.class);
        Assert.assertEquals(eventFromResponse.getId(), eventFromRepository.getId());
        Assert.assertEquals("event1", eventFromRepository.getName());
    }

    @Test
    public void addTwoValidEvents() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));

        ResponseEntity<String> response1 = testMethods.setPass("user", "password").addEvent(new Event(reservable, "event1", 200, 300));
        ResponseEntity<String> response2 = testMethods.addEvent(new Event(reservable, "event2", 450, 650));

        Assert.assertEquals(200, response1.getStatusCodeValue());
        Assert.assertEquals(200, response2.getStatusCodeValue());
        List<Event> allEvents = eventService.findAll();
        Assert.assertEquals(2, allEvents.size());

    }

    @Test
    public void getAllEvents() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        eventService.save(new Event(null, "event"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAllEvents();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Event> events = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        Assert.assertEquals(1, events.size());
        Event event = events.get(0);
        Assert.assertEquals("event", event.getName());
    }

    @Test
    public void getEventWithId() throws JsonProcessingException {
        accountService.save(new Account("user", "password"));
        eventService.save(new Event(null, "event"));
        Event expected = eventService.findAll().get(0);

        ResponseEntity<String> response = testMethods.setPass("user", "password").getEventWithId(expected.getId().toString());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void editEvent() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));
        event.setName("other");

        ResponseEntity<String> response = testMethods.setPass("user", "password").editEvent(event.getId(), event);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event eventFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Assert.assertEquals("other", eventFromResponse.getName());
    }

    @Test
    public void editEventId() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));
        UUID oldId = event.getId();
        UUID newId = UUID.randomUUID();
        JSONObject eventJson = new JSONObject(objectMapper.writeValueAsString(event));
        eventJson.put("id", newId);
        event = objectMapper.readValue(eventJson.toString(), Event.class);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editEvent(oldId, event);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
        List<Event> all = eventService.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(oldId, all.get(0).getId());
    }

    @Test
    public void deleteEvent(){
        Account account = accountService.save(new Account("user", "password"));
        adminService.save(new Admin(account));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));
        reservationService.save(new Reservation(account, event, reservable));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteEvent(event.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, eventService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(1, reservableService.findAll().size());
        Assert.assertEquals(0, reservationService.findAll().size());
    }

    @Test
    public void addSeatReservation() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservation(new Reservation(account, event, reservable));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        Reservation reservationFromRepository = reservationService.findById(reservationFromResponse.getId());
        Assert.assertEquals(account.getId(), reservationFromRepository.getAccount().getId());
        Assert.assertEquals(event.getId(), reservationFromRepository.getEvent().getId());
        Assert.assertEquals(reservable.getId(), reservationFromRepository.getReservable().getId());
    }

    @Test
    public void addSeatBadEventReservation() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        Seat seat1 = (Seat) reservableService.save(new Seat("name1"));
        Seat seat2 = (Seat) reservableService.save(new Seat("name2"));
        eventService.save(new Event(seat1, "event1", 10000000, 10000001));
        Event event2 = eventService.save(new Event(seat2 , "event2", 2000, 2000));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservation(new Reservation(account, event2, seat1));

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("reservation is invalid", jsonObject.get("message"));
        List<Reservation> reservations = reservationService.findAll();
        Assert.assertEquals(0, reservations.size());
    }

    @Test
    public void addTakenSeatReservation() throws JsonProcessingException, JSONException {
        Account account1 = accountService.save(new Account("account1", "password1"));
        Account account2 = accountService.save(new Account("account2", "password2"));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));

        ResponseEntity<String> response200 = testMethods.setPass("account1", "password1").addReservation(new Reservation(account1, event, reservable));
        ResponseEntity<String> response400 = testMethods.setPass("account2", "password2").addReservation(new Reservation(account2, event, reservable));

        Assert.assertEquals(200, response200.getStatusCodeValue());
        Reservation reservationFromResponse200 = objectMapper.readValue(Objects.requireNonNull(response200.getBody()), Reservation.class);
        Assert.assertEquals(400, response400.getStatusCodeValue());
        JSONObject jsonObjectFromResponse400 = new JSONObject(response400.getBody());
        Assert.assertEquals("reservable is taken", jsonObjectFromResponse400.get("message"));
        List<Reservation> reservations = reservationService.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservationFromRepository = reservations.get(0);
        Assert.assertEquals(reservationFromRepository.getId(), reservationFromResponse200.getId());
        Assert.assertEquals(account1.getId(), reservationFromRepository.getAccount().getId());
    }

    @Test
    public void addSpaceReservation() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space = (Space) reservableService.save(new Space("space1"));
        Reservable reservable = reservableService.save(new Seat("name", space));
        Event event = eventService.save(new Event(space, "event"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservation(new Reservation(account, event, reservable));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        List<Reservation> reservations = reservationService.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservationFromRepository = reservations.get(0);
        Assert.assertEquals(reservationFromRepository.getId(), reservationFromResponse.getId());
        Assert.assertEquals(account.getId(), reservationFromRepository.getAccount().getId());
    }

    @Test
    public void addReservationOnSeatInReservedSpace() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        Space space = (Space) reservableService.save(new Space("space1"));
        Seat seat = (Seat) reservableService.save(new Seat("seat1", space));
        Event event = eventService.save(new Event(space, "event"));
        reservationService.save(new Reservation(account, event, space));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservation(new Reservation(account, event, seat));

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("reservable is taken", jsonObject.get("message"));
    }


    @Test
    public void editReservation() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space1 = (Space) reservableService.save(new Space("space1"));
        Reservable reservable1 = reservableService.save(new Seat("name1", space1));
        Reservable reservable2 = reservableService.save(new Seat("name2", space1));
        Event event = eventService.save(new Event(space1, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable1));
        reservation.setReservable(reservable2);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservation(reservation.getId(), reservation);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        Assert.assertEquals(reservable2.getId(), reservationFromResponse.getReservable().getId());
    }

    @Test
    public void editIdReservation() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        Seat seat = (Seat) reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(seat, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, seat));
        UUID oldId = reservation.getId();
        UUID newId = UUID.randomUUID();
        JSONObject reservationJson = new JSONObject(objectMapper.writeValueAsString(reservation));
        reservationJson.put("id", newId);
        reservation = objectMapper.readValue(reservationJson.toString(), Reservation.class);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservation(oldId, reservation);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
    }

    @Test
    public void deleteReservation() {
        Account account = accountService.save(new Account("user", "password"));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable));
        Assert.assertEquals(1, reservationService.findAll().size());

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservation(reservation.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, reservationService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(1, reservableService.findAll().size());
        Assert.assertEquals(1, eventService.findAll().size());
    }
}
