package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.types.Seat;
import com.example.ReservationApi.reservation.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.*;

class TestMethods {
    private TestRestTemplate testRestTemplate;
    private ObjectMapper mapper = new ObjectMapper();
    private String username;
    private String password;
    private HttpHeaders headers;

    TestMethods(String username, String password, TestRestTemplate testRestTemplate){
        this.testRestTemplate = testRestTemplate;
        this.username = username;
        this.password = password;

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }


    void addAccount(Account account, String password) throws JSONException {
        JSONObject accountJsonObject = new JSONObject();
        accountJsonObject.put("login", account.getLogin());
        accountJsonObject.put("password", password);

        HttpEntity<String> accountAddRequest = new HttpEntity<>(accountJsonObject.toString(), headers);
        ResponseEntity<String> accountAddResponse = testRestTemplate.postForEntity("/api/account", accountAddRequest, String.class);
        Assert.assertEquals(200, accountAddResponse.getStatusCode().value());
    }

    Account getAccount(UUID id) throws JsonProcessingException {
        ResponseEntity<String> getAccountWitIdResponse = testRestTemplate.withBasicAuth(username, password).getForEntity("/api/account/" + id, String.class);
        Assert.assertEquals(200, getAccountWitIdResponse.getStatusCodeValue());

        Map<String, String> accountJsonObject = mapper.readValue(Objects.requireNonNull(getAccountWitIdResponse.getBody()), new TypeReference<Map<String,String>>(){});
        Account account = new Account(UUID.fromString(accountJsonObject.get("id")), accountJsonObject.get("login"), "");
        List<String> eventsIdsStrings = mapper.readValue(accountJsonObject.get("eventsIds"), new TypeReference<>(){});

        for(String eventId: eventsIdsStrings){
            account.getEvents().add(new Event(UUID.fromString(eventId), account, null, ""));
        }

        List<String> reservationsIdsStrings = mapper.readValue(accountJsonObject.get("reservationsIds"), new TypeReference<>(){});
        for(String reservationId: reservationsIdsStrings){
            account.getReservations().add(new Reservation(UUID.fromString(reservationId)));
        }

        Assert.assertEquals(account.getId(), id);
        return account;
    }


    void addOneSeat(Seat seat) throws JSONException {
        JSONObject seatJson = new JSONObject();
        seatJson.put("type", "Seat");
        seatJson.put("name", seat.getName());

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(seatJson);

        HttpEntity<String> addSeatsRequest = new HttpEntity<>(jsonArray.toString(), headers);
        ResponseEntity<String> addSeatsResponse = testRestTemplate.withBasicAuth(username, password).postForEntity("/api/reservable", addSeatsRequest, String.class);
        Assert.assertEquals(200, addSeatsResponse.getStatusCode().value());

    }

    Reservable[] getAllReservableObjects() throws JsonProcessingException {
        ResponseEntity<String> getReservableObjectsResponse = testRestTemplate.withBasicAuth(username, password).getForEntity("/api/reservable", String.class);
        Assert.assertEquals(200, getReservableObjectsResponse.getStatusCodeValue());

        Map<String, String>[] jsonObjects = mapper.readValue(Objects.requireNonNull(getReservableObjectsResponse.getBody()), new TypeReference<Map<String,String>[]>(){});
        List<Reservable> reservableList = new ArrayList<>();
        for(Map<String, String> jsonObject: jsonObjects){
            if (jsonObject.get("type").equals("Seat"))
                jsonObject.remove("type");
            reservableList.add(mapper.convertValue(jsonObject, Seat.class));
        }
        return reservableList.toArray(new Reservable[0]);
    }

    void addEvent(Event event) throws JSONException {
        JSONObject eventJson = new JSONObject();
        eventJson.put("name", event.getName());
        eventJson.put("accountId", event.getAccount().getId());

        HttpEntity<String> addEventRequest = new HttpEntity<>(eventJson.toString(), headers);
        ResponseEntity<String> addEventResponse = testRestTemplate.withBasicAuth(username, password).postForEntity("/api/event", addEventRequest, String.class);
        Assert.assertEquals(200, addEventResponse.getStatusCode().value());
    }

    Event[] getAllEvents() throws JsonProcessingException {
        ResponseEntity<String> getAllEventsResponse = testRestTemplate.withBasicAuth(username, password).getForEntity("/api/event", String.class);
        Assert.assertEquals(200, getAllEventsResponse.getStatusCodeValue());

        Map<String, String>[] eventJsonObjects = mapper.readValue(Objects.requireNonNull(getAllEventsResponse.getBody()), new TypeReference<Map<String,String>[]>(){});
        List<Event> eventsList = new ArrayList<>();
        for(Map<String, String> eventMap: eventJsonObjects){
            Event event = this.readEventMap(eventMap);
            eventsList.add(event);
        }
        return eventsList.toArray(new Event[0]);

    }

    Event getEventWithId(String eventId) throws JsonProcessingException {
        ResponseEntity<String> getAllEventsResponse = testRestTemplate.withBasicAuth(username, password).getForEntity("/api/event/" + eventId, String.class);
        Assert.assertEquals(200, getAllEventsResponse.getStatusCodeValue());
        Map<String, String> eventMap = mapper.readValue(Objects.requireNonNull(getAllEventsResponse.getBody()), new TypeReference<Map<String,String>>(){});
        return this.readEventMap(eventMap);
    }

    private Event readEventMap(Map<String, String> eventMap) {
        String accountId = eventMap.get("accountId");
        eventMap.remove("accountId");
        Event event = mapper.convertValue(eventMap, Event.class);
        event.setAccount(new Account(UUID.fromString(accountId)));
        return event;
    }

    void addReservation(Account account, Event event, Reservable reservable) throws JSONException {
        JSONObject reservationJsonObject = new JSONObject();
        reservationJsonObject.put("accountId", account.getId());
        reservationJsonObject.put("eventId", event.getId());
        reservationJsonObject.put("reservableId", reservable.getId());

        HttpEntity<String> addReservationRequest = new HttpEntity<>(reservationJsonObject.toString(), headers);
        ResponseEntity<String> addReservationResponse = testRestTemplate.withBasicAuth(username, password).postForEntity("/api/reservation", addReservationRequest, String.class);
    }
}
