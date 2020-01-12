package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
import com.example.reservationApi.authentication.Token.Token;
import com.example.reservationApi.authentication.Token.TokenService;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.event.EventService;
import com.example.reservationApi.json.ConfiguredMapper;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.ReservableService;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.example.reservationApi.reservation.ReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UnitTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    AccountService accountService;

    @Autowired
    TokenService tokenService;

    @Autowired
    EventService eventService;

    @Autowired
    ReservationService reservationService;

    @Autowired
    private ReservableService reservableService;

    private TestMethods testMethods;


    @Before
    public void before() {
        testMethods = new TestMethods(testRestTemplate);
    }

    @Test
    public void tokenAuthorization() {
        String token = "token.token.token.tok";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> notAuthorizedGetAccountsResponse = testRestTemplate.exchange("/api/account", HttpMethod.GET, request, String.class);
        Assert.assertEquals(401, notAuthorizedGetAccountsResponse.getStatusCodeValue());

        Account account = new Account("user", "password");
        accountService.save(account);
        tokenService.save(new Token(token, account));

        ResponseEntity<String> authorizedGetAccountsResponse = testRestTemplate.exchange("/api/account", HttpMethod.GET, request, String.class);
        Assert.assertEquals(200, authorizedGetAccountsResponse.getStatusCodeValue());

    }

    @Test
    public void loginPasswordAuthorization() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("user", "password");
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> notAuthorizedGetAccountsResponse = testRestTemplate.exchange("/api/account", HttpMethod.GET, request,  String.class);
        Assert.assertEquals(401, notAuthorizedGetAccountsResponse.getStatusCodeValue());

        Account account = new Account("user", "password");
        accountService.save(account);

        ResponseEntity<String> authorizedGetAccountsResponse = testRestTemplate.exchange("/api/account", HttpMethod.GET, request,  String.class);
        Assert.assertEquals(200, authorizedGetAccountsResponse.getStatusCodeValue());

    }

    @Test
    public void generateToken() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        ResponseEntity<String> response = testMethods.setPass("user", "password").generateToken();

        Assert.assertEquals(200, response.getStatusCodeValue());
        Token token = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Token.class);
        Assert.assertEquals(21, token.getToken().length());
        Assert.assertEquals(account.getId(), token.getAccount().getId());
    }

    @Test
    public void addAccount() throws JsonProcessingException, JSONException {
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
    public void getAccountById() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAccountById(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals(account.getId(), actual.getId());
        Assert.assertEquals(account.getLogin(), actual.getLogin());
    }

    @Test
    public void getAccount404(){
        accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAccountById(UUID.fromString("7c520301-7310-4e39-b36d-2f0f808ad38d"));

        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void getAllAccounts() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        Account account2 = accountService.save(new Account("login2", "password"));

        ResponseEntity<String> response = testMethods.setPass("login2", "password").getAllAccounts();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Account> accounts = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {
        });
        List<UUID> uuidList = accounts.stream().map(Account::getId).collect(Collectors.toList());
        Assert.assertTrue(uuidList.contains(account.getId()));
        Assert.assertTrue(uuidList.contains(account2.getId()));
        JSONArray jsonAccounts = new JSONArray(response.getBody());
        Assert.assertFalse(jsonAccounts.getJSONObject(0).has("password"));
    }

    @Test
    public void getAccountsByLogin() throws Exception {
        Account account = accountService.save(new Account("user", "password"));
        Account account2 = accountService.save(new Account("login2", "password"));

        Map<String, String> filters = new HashMap<>();
        filters.put("login", "login2");
        ResponseEntity<String> response = testMethods.setPass("login2", "password").getAllAccountsFiltered(filters);

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Account> accounts = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {
        });
        List<UUID> uuidList = accounts.stream().map(Account::getId).collect(Collectors.toList());
        Assert.assertFalse(uuidList.contains(account.getId()));
        Assert.assertTrue(uuidList.contains(account2.getId()));
    }

    @Test
    public void getAccountsByLoginNotFoundAny() throws Exception {
        accountService.save(new Account("login2", "password"));

        Map<String, String> filters = new HashMap<>();
        filters.put("login", "login3");
        ResponseEntity<String> response = testMethods.setPass("login2", "password").getAllAccountsFiltered(filters);

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Account> accounts = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {
        });
        Assert.assertEquals(0, accounts.size());
    }

    @Test
    public void editAccount() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("login","newLogin");
        accountMap.put("password","newPassword");
        accountMap.put("id", account.getId());

        ResponseEntity<String> response = testMethods.setPass("user", "password").editAccount(account.getId(), accountMap);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Account updatedAccount = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Account.class);
        Assert.assertEquals("newLogin", updatedAccount.getLogin());
        updatedAccount = accountService.findById(updatedAccount.getId());
        Assert.assertEquals("newPassword", updatedAccount.getPassword());
    }

    @Test
    public void editAccountByOtherAccount() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        accountService.save(new Account("user2", "password2"));
        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("login", "newLogin");
        accountMap.put("password","newPassword");

        ResponseEntity<String> response = testMethods.setPass("user2", "password2").editAccount(account.getId(), accountMap);

        Assert.assertEquals(403, response.getStatusCodeValue());
        account = accountService.findById(account.getId());
        Assert.assertEquals("user", account.getLogin());
        Assert.assertEquals("password", account.getPassword());

    }

    @Test
    public void editAccountByAdmin() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Account other = accountService.save(new Account("user2", "password2", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("login", "newLogin");

        ResponseEntity<String> response = testMethods.setPass("user2", "password2").editAccount(account.getId(), accountMap);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals("newLogin", accountService.findById(account.getId()).getLogin());
    }

    @Test
    public void editIdAccount() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password"));
        UUID oldId = account.getId();
        UUID newId = UUID.randomUUID();

        HashMap<String, Object> accountMap = new HashMap<>();
        accountMap.put("id", newId);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editAccount(oldId, accountMap);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
        List<Account> all = accountService.findAll();
        Assert.assertEquals(1, accountService.findAll().size());
        Assert.assertEquals(oldId, all.get(0).getId());
    }

    @Test
    public void deleteAccount(){
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        tokenService.save(new Token("token", account));
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "Event1"));
        reservationService.save(new Reservation(account, event, reservable));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteAccount(account.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, accountService.findAll().size());
        Assert.assertNull(tokenService.getByToken("token"));
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
    public void addSpace() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
    public void getReservable404(){
        accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getReservableById(UUID.fromString("7c520301-7310-4e39-b36d-2f0f808ad38d"));

        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void editReservable() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        Seat seat = (Seat) reservableService.save(new Seat("name"));
        HashMap<String, Object> seatMap = new HashMap<>();
        seatMap.put("name", "seat");

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservable(seat.getId(), seatMap);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservable reservable = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservable.class);
        Assert.assertEquals("seat", reservable.getName());
    }

    @Test
    public void editIdReservable() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        Seat seat = (Seat) reservableService.save(new Seat("name"));
        UUID oldId = seat.getId();
        UUID newId = UUID.randomUUID();

        HashMap<String, Object> seatMap = new HashMap<>();
        seatMap.put("id", newId);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservable(oldId, seatMap);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
    }

    @Test
    public void deleteReservable(){
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        Space space = (Space) reservableService.save(new Space("space"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").deleteReservable(space.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals(0, reservableService.findAll().size());
        Assert.assertEquals(1, accountService.findAll().size());
    }

    @Test
    public void deleteSpace() throws JSONException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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

        ResponseEntity<String> response = testMethods.setPass("user", "password").getEventById(expected.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event actual = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Assert.assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void getEvent404(){
        accountService.save(new Account("user", "password"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getEventById(UUID.fromString("7c520301-7310-4e39-b36d-2f0f808ad38d"));

        Assert.assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    public void editEvent() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", "other");
        updateMap.put("id", event.getId());

        ResponseEntity<String> response = testMethods.setPass("user", "password").editEvent(event.getId(), updateMap);

        Assert.assertEquals(200, response.getStatusCodeValue());
        Event eventFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Event.class);
        Assert.assertEquals("other", eventFromResponse.getName());
    }

    @Test
    public void editEventId() throws JsonProcessingException, JSONException {
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
        Reservable reservable = reservableService.save(new Seat("name"));
        Event event = eventService.save(new Event(reservable, "event"));
        UUID oldId = event.getId();
        UUID newId = UUID.randomUUID();

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("id", newId);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editEvent(oldId, updateMap);

        Assert.assertEquals(400, response.getStatusCodeValue());
        JSONObject jsonObject = new JSONObject(response.getBody());
        Assert.assertEquals("id is unchangable", jsonObject.get("message"));
        List<Event> all = eventService.findAll();
        Assert.assertEquals(1, all.size());
        Assert.assertEquals(oldId, all.get(0).getId());
    }

    @Test
    public void deleteEvent(){
        Account account = accountService.save(new Account("user", "password", new ArrayList<>() {{
            add("ROLE_ADMIN");
        }}));
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
    public void addSeatInSpaceInOtherSpaceReservation() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space1 = (Space) reservableService.save(new Space("name"));
        Space space2 = (Space) reservableService.save(new Space("name", space1));
        Reservable reservable = reservableService.save(new Seat("name", space2));
        Event event = eventService.save(new Event(space1, "event"));

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
        Event event = eventService.save(new Event(space, "event"));

        ResponseEntity<String> response = testMethods.setPass("user", "password").addReservation(new Reservation(account, event, space));

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
    public void getReservationById() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space1 = (Space) reservableService.save(new Space("space1"));
        Reservable reservable1 = reservableService.save(new Seat("name1", space1));
        Reservable reservable2 = reservableService.save(new Seat("name2", space1));
        Event event = eventService.save(new Event(space1, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable1));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getReservationById(reservation.getId());

        Assert.assertEquals(200, response.getStatusCodeValue());
        Reservation reservationFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), Reservation.class);
        Assert.assertEquals(reservation.getId(), reservationFromResponse.getId());
        Assert.assertEquals(reservation.getEvent().getId(), reservationFromResponse.getEvent().getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservationFromResponse.getReservable().getId());
        Assert.assertEquals(reservation.getAccount().getId(), reservationFromResponse.getAccount().getId());
    }

    @Test
    public void getAllReservations() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space1 = (Space) reservableService.save(new Space("space1"));
        Reservable reservable1 = reservableService.save(new Seat("name1", space1));
        Event event = eventService.save(new Event(space1, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable1));

        ResponseEntity<String> response = testMethods.setPass("user", "password").getAllReservations();

        Assert.assertEquals(200, response.getStatusCodeValue());
        List<Reservation> reservationsFromResponse = objectMapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>(){});
        Reservation reservationFromResponse = reservationsFromResponse.get(0);
        Assert.assertEquals(reservation.getId(), reservationFromResponse.getId());
    }

    @Test
    public void editReservation() throws JsonProcessingException {
        Account account = accountService.save(new Account("user", "password"));
        Space space1 = (Space) reservableService.save(new Space("space1"));
        Reservable reservable1 = reservableService.save(new Seat("name1", space1));
        Reservable reservable2 = reservableService.save(new Seat("name2", space1));
        Event event = eventService.save(new Event(space1, "event"));
        Reservation reservation = reservationService.save(new Reservation(account, event, reservable1));

        HashMap<String, Object> updateMap = new HashMap<>();

        ObjectMapper mapper = new ConfiguredMapper();

        updateMap.put("reservable", reservable2.getId());
        updateMap.put("id", reservation.getId());

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservation(reservation.getId(), updateMap);

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

        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("id", newId);

        ResponseEntity<String> response = testMethods.setPass("user", "password").editReservation(oldId, updateMap);

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