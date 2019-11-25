package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountRepository;
import com.example.reservationApi.admin.Admin;
import com.example.reservationApi.admin.AdminRepository;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventRepository;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableRepository;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationRepository;
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
import org.springframework.data.domain.Example;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UnitTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    private ReservableRepository reservableRepository;

    private TestMethods testMethods;


    @Before
    public void before(){
        testMethods = new TestMethods(testRestTemplate);
        reservableRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        adminRepository.deleteAll();
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
    public void inheritedEntityRepository(){
        reservableRepository.save(new Seat("A1"));
        Assert.assertEquals(1, reservableRepository.findAll().size());
        Assert.assertEquals(Seat.class, reservableRepository.findAll().get(0).getClass());
    }

    @Test
    public void accountJsonConverter() throws JsonProcessingException {
        Reservable reservable = reservableRepository.save(new Seat("seat1"));
        Event event = eventRepository.save(new Event(reservable, "event1"));
        Account account1 = accountRepository.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationRepository.save(new Reservation(account1, event, reservable));
        account1 = accountRepository.findById(account1.getId()).orElseThrow();

        String account1JsonString = objectMapper.writeValueAsString(account1);
        Account account1FromJson = objectMapper.readValue(account1JsonString, Account.class);

        Assert.assertEquals(account1.getId(), account1FromJson.getId());
        Assert.assertEquals(account1.getLogin(), account1FromJson.getLogin());
        Assert.assertNotNull(account1.getReservations());
        Assert.assertEquals(1, account1FromJson.getReservations().size());
        Assert.assertEquals(reservation.getId(), account1FromJson.getReservations().get(0).getId());

    }

    @Test
    public void eventJsonConvert() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Seat seat1 = new Seat("seat1");
        reservableRepository.save(seat1);
        seat1 = (Seat) reservableRepository.findAll().get(0);
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
        Reservable reservable = reservableRepository.save(new Seat("seat1"));;
        Event event = eventRepository.save(new Event(reservable, "event1"));
        Account account1 = accountRepository.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationRepository.save(new Reservation(account1, event, reservable));

        String reservationJsonString = objectMapper.writeValueAsString(reservation);
        Reservation reservationFromJson = objectMapper.readValue(reservationJsonString, Reservation.class);

        Assert.assertEquals(reservation.getId(), reservationFromJson.getId());
        Assert.assertEquals(reservation.getAccount().getId(), reservationFromJson.getAccount().getId());
        Assert.assertEquals(reservation.getEvent().getId(), reservationFromJson.getEvent().getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservationFromJson.getReservable().getId());

    }

    @Test
    public void SeatJsonConverter() throws JsonProcessingException {
        Space space = reservableRepository.save(new Space("space1"));
        Reservable reservable = reservableRepository.save(new Seat("seat1", space));
        Event event = eventRepository.save(new Event(reservable, "event1"));
        Account account1 = accountRepository.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationRepository.save(new Reservation(account1, event, reservable));
        reservable = reservableRepository.findById(reservable.getId()).orElseThrow();

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
        Space space = reservableRepository.save(new Space("space1"));
        reservableRepository.save(new Seat("seat1", space));
        space = (Space) reservableRepository.findById(space.getId()).orElseThrow();

        String spaceJsonString = objectMapper.writeValueAsString(space);
        Space spaceFromJson = (Space) objectMapper.readValue(spaceJsonString, Reservable.class);

        Assert.assertEquals(space.getId(), spaceFromJson.getId());
        Assert.assertNotNull(spaceFromJson.getReservables());
        List<Reservable> spaceReservableList = space.getReservables();
        List<Reservable> spaceFromJsonReservableList = spaceFromJson.getReservables();
        Assert.assertEquals(spaceReservableList.size(), spaceFromJsonReservableList.size());
        Assert.assertEquals(spaceReservableList.get(0).getId(), spaceFromJsonReservableList.get(0).getId());
    }

    @Test
    public void addAccount() throws JsonProcessingException {
        ResponseEntity<String> response = testMethods.addAccount(new Account("user", "password"));
        Account addedAccount = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);

        List<Account> accounts = accountRepository.findAll();
        Assert.assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        Assert.assertEquals("user", account.getLogin());
        Assert.assertEquals(account.getLogin(), addedAccount.getLogin());
        Assert.assertEquals(account.getId(), addedAccount.getId());
    }

    @Test
    public void getAccount() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.getAccount(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals(account.getId(), actual.getId());
        Assert.assertEquals(account.getLogin(), actual.getLogin());
    }

    @Test
    public void addAccountsWithSameLogin() throws JsonProcessingException {
        ResponseEntity<String> response1 = testMethods.addAccount(new Account("login", "password"));
        Account accountAdded = accountRepository.findAll().get(0);
        ResponseEntity<String> response2 = testMethods.addAccount(new Account("login", "other"));
        Assert.assertNull(response2.getBody());

        List<Account> all = accountRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(accountAdded.getId(), all.get(0).getId());
    }

    @Test
    public void getAllAccounts() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Account account2 = accountRepository.save(new Account("login2", "password"));

        ResponseEntity<String> response = testMethods.getAllAccounts();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Account> accounts = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        List<UUID> uuidList = accounts.stream().map(Account::getId).collect(Collectors.toList());
        Assert.assertTrue(uuidList.contains(account.getId()));
        Assert.assertTrue(uuidList.contains(account2.getId()));
    }

    @Test
    public void editAccount() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        account.setLogin("newLogin");

        ResponseEntity<String> response = testMethods.editAccount(account.getId(), account);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account updatedAccount = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals(account.getLogin(), updatedAccount.getLogin());
    }

    @Test
    public void editIdAccount() throws JsonProcessingException, JSONException {
        Account account = accountRepository.save(new Account("user", "password"));
        UUID oldId = account.getId();
        UUID newId = UUID.randomUUID();
        JSONObject account1Json = new JSONObject(objectMapper.writeValueAsString(account));
        account1Json.put("id", newId);
        account = objectMapper.readValue(account1Json.toString(), Account.class);
        ResponseEntity<String> response = testMethods.editAccount(oldId, account);

        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertNull(response.getBody());
        List<Account> all = accountRepository.findAll();
        Assert.assertEquals(1, accountRepository.findAll().size());
        Assert.assertEquals(oldId ,all.get(0).getId());
    }

    @Test
    public void deleteAccount(){
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save((new Admin(account)));
        Assert.assertEquals(1, accountRepository.findAll().size());

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteAccount(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, accountRepository.findAll().size());
    }

    @Test
    public void deleteAccountWrong(){
        Account account = accountRepository.save(new Account("user", "password"));
        Assert.assertEquals(1, accountRepository.findAll().size());

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteAccount(account.getId());

        Assert.assertEquals(403, response.getStatusCodeValue());
        Assert.assertEquals(1, accountRepository.findAll().size());
    }

    @Test
    public void addAdmin() throws JsonProcessingException {
        Account adminAccount = accountRepository.save(new Account("admin", "password"));
        adminRepository.save(new Admin(adminAccount));
        Account account = accountRepository.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("admin", "password").addAdmin(new Admin(account));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Admin admin = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Admin.class);
        Assert.assertEquals(account.getId(), admin.getAccount().getId());
    }

    @Test
    public void addAdminWrong() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addAdmin(new Admin(account));

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addSpace() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Space space = new Space("space");

        ResponseEntity<String> response = testMethods.addReservable(space);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Space addedSpace = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Space.class);
        Assert.assertEquals(space.getName(), addedSpace.getName());
        reservableRepository.findOne(Example.of(new Space("space"))).orElseThrow();
    }

    @Test
    public void addSpaceWithoutAdmin() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Space space = new Space("space");

        ResponseEntity<String> response = testMethods.addReservable(space);

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addSeatWithSpace() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Space space = reservableRepository.save(new Space("space"));

        ResponseEntity<String> response = testMethods.addReservable(new Seat("name", space));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Seat seat = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Seat.class);
        Assert.assertEquals("name", seat.getName());
        Assert.assertEquals(seat.getSpace().getId(), space.getId());
        reservableRepository.findOne(Example.of(new Seat("name"))).orElseThrow();
    }

    @Test
    public void getAllReservableObjects() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.getAllReservableObjects();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Reservable> allReservableObjects = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        Seat expected = (Seat) reservableRepository.findAll().get(0);
        Assert.assertEquals(1, allReservableObjects.size());
        Seat actual = (Seat) allReservableObjects.get(0);
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void getReservableById() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Seat seat = reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.getReservableById(seat.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservable reservable = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservable.class);
        Assert.assertEquals("name", reservable.getName());
    }

    @Test
    public void editReservable() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Seat seat = reservableRepository.save(new Seat("name"));
        seat.setName("seat");

        ResponseEntity<String> response = testMethods.editReservable(seat.getId(), seat);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservable reservable = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservable.class);
        Assert.assertEquals("seat", reservable.getName());
    }

    @Test
    public void editIdReservable() throws JsonProcessingException, JSONException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Seat seat = reservableRepository.save(new Seat("name"));
        UUID oldId = seat.getId();
        UUID newId = UUID.randomUUID();
        JSONObject seatJson = new JSONObject(objectMapper.writeValueAsString(seat));
        seatJson.put("id", newId);
        seat = objectMapper.readValue(seatJson.toString(), Seat.class);

        ResponseEntity<String> response = testMethods.editReservable(oldId, seat);

        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertNull(response.getBody());
    }

    @Test
    public void deleteReservable(){
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Space space = reservableRepository.save(new Space("space"));
        Assert.assertEquals(1, reservableRepository.findAll().size());

        ResponseEntity<String> response = testMethods.deleteReservable(space.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, reservableRepository.findAll().size());
        Assert.assertEquals(1, accountRepository.findAll().size());
    }

    @Test
    public void addEvent() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.addEvent(new Event(reservable, "event"));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event addedEvent = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        List<Event> allEvents = eventRepository.findAll();
        Assert.assertEquals(1, allEvents.size());
        Event event = allEvents.get(0);
        Assert.assertEquals(event.getId(), addedEvent.getId());
        Assert.assertEquals("event", event.getName());
        Assert.assertNotEquals(null, event.getReservable());
        Assert.assertEquals(reservable.getId(), event.getReservable().getId());
    }

    @Test
    public void addEventWithoutAdmin() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Reservable reservable = reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response = testMethods.addEvent(new Event(reservable, "event"));

        Assert.assertEquals(403, response.getStatusCodeValue());
    }

    @Test
    public void addTwoEventsOnSameReservbleSameTime() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response200 = testMethods.addEvent(new Event(reservable, "event1", 200, 300));
        ResponseEntity<String> response400 = testMethods.addEvent(new Event(reservable, "event2", 250, 350));

        Assert.assertEquals(200, response200.getStatusCodeValue());
        Assert.assertEquals(400, response400.getStatusCodeValue());
        List<Event> allEvents = eventRepository.findAll();
        Assert.assertEquals(1, allEvents.size());
        Event eventFromRepository = allEvents.get(0);
        Event eventFromResponse = objectMapper.readValue(Objects.requireNonNull(response200.getBody()), Event.class);
        Assert.assertEquals(eventFromResponse.getId(), eventFromRepository.getId());
        Assert.assertEquals("event1", eventFromRepository.getName());
    }

    @Test
    public void addTwoValidEvents() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));

        ResponseEntity<String> response1 = testMethods.addEvent(new Event(reservable, "event1", 200, 300));
        ResponseEntity<String> response2 = testMethods.addEvent(new Event(reservable, "event2", 450, 650));

        Assert.assertEquals(200, response1.getStatusCodeValue());
        Assert.assertEquals(200, response2.getStatusCodeValue());
        List<Event> allEvents = eventRepository.findAll();
        Assert.assertEquals(2, allEvents.size());

    }

    @Test
    public void getAllEvents() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        eventRepository.save(new Event(null, "event"));

        ResponseEntity<String> response = testMethods.getAllEvents();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Event> events = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});
        Assert.assertEquals(1, events.size());
        Event event = events.get(0);
        Assert.assertEquals("event", event.getName());
    }

    @Test
    public void getEventWithId() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        eventRepository.save(new Event(null, "event"));
        Event expected = eventRepository.findAll().get(0);

        ResponseEntity<String> response = testMethods.getEventWithId(expected.getId().toString());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void editEvent() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));
        event.setName("other");

        ResponseEntity<String> response = testMethods.editEvent(event.getId(), event);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event eventFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        Assert.assertEquals("other", updatedEvent.getName());
    }

    @Test
    public void editEventId() throws JsonProcessingException, JSONException {
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));
        UUID oldId = event.getId();
        UUID newId = UUID.randomUUID();
        JSONObject eventJson = new JSONObject(objectMapper.writeValueAsString(event));
        eventJson.put("id", newId);
        event = objectMapper.readValue(eventJson.toString(), Event.class);

        ResponseEntity<String> response = testMethods.editEvent(oldId, event);

        Assert.assertEquals(400, response.getStatusCodeValue());
        List<Event> all = eventRepository.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(oldId, all.get(0).getId());
    }

    @Test
    public void deleteEvent(){
        Account account = accountRepository.save(new Account("user", "password"));
        Admin admin = adminRepository.save(new Admin(account));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));
        Assert.assertEquals(1, eventRepository.findAll().size());

        ResponseEntity<String> response = testMethods.deleteEvent(event.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, eventRepository.findAll().size());
        Assert.assertEquals(1, accountRepository.findAll().size());
        Assert.assertEquals(1, reservableRepository.findAll().size());
    }

    @Test
    public void addSeatReservation() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));

        ResponseEntity<String> response = testMethods.addReservation(new Reservation(account, event, reservable));

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservationFromRepository = reservations.get(0);
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        Assert.assertEquals(reservationFromRepository.getId(), reservationFromResponse.getId());
        Assert.assertEquals(account.getId(), reservationFromRepository.getAccount().getId());
        Assert.assertEquals(event.getId(), reservationFromRepository.getEvent().getId());
        Assert.assertEquals(reservable.getId(), reservationFromRepository.getReservable().getId());
    }

    @Test
    public void addSeatBadEventReservation() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Seat seat1 = reservableRepository.save(new Seat("name1"));
        Seat seat2 = reservableRepository.save(new Seat("name2"));
        eventRepository.save(new Event(seat1, "event1", 10000000, 10000001));
        Event event2 = eventRepository.save(new Event(seat2 , "event2", 2000, 2000));

        ResponseEntity<String> response = testMethods.addReservation(new Reservation(account, event2, seat1));

        Assert.assertEquals(400, response.getStatusCodeValue());
        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(0, reservations.size());
    }

    @Test
    public void addTakenSeatReservation() throws JsonProcessingException {
        Account account1 = accountRepository.save(new Account("account1", "password1"));
        Account account2 = accountRepository.save(new Account("account2", "password2"));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));

        ResponseEntity<String> response200 = testMethods.setPass("account1", "password1").addReservation(new Reservation(account1, event, reservable));
        ResponseEntity<String> response400 = testMethods.setPass("account2", "password2").addReservation(new Reservation(account2, event, reservable));

        Assert.assertEquals(200, response200.getStatusCodeValue());
        Reservation reservationFromResponse200 = objectMapper.readValue(Objects.requireNonNull(response200.getBody()), Reservation.class);
        Assert.assertEquals(400, response400.getStatusCodeValue());
        Assert.assertNull(response400.getBody());
        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservationFromRepository = reservations.get(0);
        Assert.assertEquals(reservationFromRepository.getId(), reservationFromResponse200.getId());
        Assert.assertEquals(account1.getId(), reservationFromRepository.getAccount().getId());
    }

    @Test
    public void addSpaceReservation() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Space space = reservableRepository.save(new Space("space1"));
        Reservable reservable = reservableRepository.save(new Seat("name", space));
        Event event = eventRepository.save(new Event(space, "event"));

        ResponseEntity<String> response = testMethods.addReservation(new Reservation(account, event, reservable));

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        List<Reservation> reservations = reservationRepository.findAll();
        Assert.assertEquals(1, reservations.size());
        Reservation reservationFromRepository = reservations.get(0);
        Assert.assertEquals(reservationFromRepository.getId(), reservationFromResponse.getId());
        Assert.assertEquals(account.getId(), reservationFromRepository.getAccount().getId());
    }

    @Test
    public void addReservationOnSeatInReservedSpace() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Space space = reservableRepository.save(new Space("space1"));
        Seat seat = reservableRepository.save(new Seat("seat1", space));
        Event event = eventRepository.save(new Event(space, "event"));
        Reservation reservation = reservationRepository.save(new Reservation(account, event, space));

        ResponseEntity<String> response = testMethods.addReservation(new Reservation(account, event, seat));

        Assert.assertEquals(400, response.getStatusCodeValue());
    }


    @Test
    public void editReservation() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Space space1 = reservableRepository.save(new Space("space1"));
        Reservable reservable1 = reservableRepository.save(new Seat("name1", space1));
        Reservable reservable2 = reservableRepository.save(new Seat("name2", space1));
        Event event = eventRepository.save(new Event(space1, "event"));
        Reservation reservation = reservationRepository.save(new Reservation(account, event, reservable1));
        reservation.setReservable(reservable2);

        ResponseEntity<String> response = testMethods.editReservation(reservation.getId(), reservation);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        Assert.assertEquals(reservable2.getId(), reservationFromResponse.getReservable().getId());
    }

    @Test
    public void editIdReservation() throws JsonProcessingException, JSONException {
        Account account = accountRepository.save(new Account("user", "password"));
        Seat seat = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(seat, "event"));
        Reservation reservation = reservationRepository.save(new Reservation(account, event, seat));
        UUID oldId = reservation.getId();
        UUID newId = UUID.randomUUID();
        JSONObject reservationJson = new JSONObject(objectMapper.writeValueAsString(reservation));
        reservationJson.put("id", newId);
        reservation = objectMapper.readValue(reservationJson.toString(), Reservation.class);

        ResponseEntity<String> response = testMethods.editReservation(oldId, reservation);

        Assert.assertEquals(400, response.getStatusCodeValue());
        Assert.assertNull(response.getBody());
    }

    @Test
    public void deleteReservation() throws JsonProcessingException {
        Account account = accountRepository.save(new Account("user", "password"));
        Reservable reservable = reservableRepository.save(new Seat("name"));
        Event event = eventRepository.save(new Event(reservable, "event"));
        Reservation reservation = reservationRepository.save(new Reservation(account, event, reservable));
        Assert.assertEquals(1, reservationRepository.findAll().size());

        ResponseEntity<String> response = testMethods.deleteReservation(reservation.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, reservationRepository.findAll().size());
        Assert.assertEquals(1, accountRepository.findAll().size());
        Assert.assertEquals(1, reservableRepository.findAll().size());
        Assert.assertEquals(1, eventRepository.findAll().size());
    }
}
