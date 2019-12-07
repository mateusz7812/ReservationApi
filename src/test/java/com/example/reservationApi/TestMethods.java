package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class TestMethods {
    private TestRestTemplate testRestTemplate;
    private ObjectMapper mapper = new ObjectMapper();
    private String username;
    private String password;
    private HttpHeaders headers;

    TestMethods(TestRestTemplate testRestTemplate){
        this.testRestTemplate = testRestTemplate;

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    TestMethods setPass(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    ResponseEntity<String> addAccount(Account account) throws JsonProcessingException, JSONException {
        String accountJson = mapper.writeValueAsString(account);
        JSONObject jsonObject = new JSONObject(accountJson);
        jsonObject.put("password", account.getPassword());
        HttpEntity<String> accountAddRequest = new HttpEntity<>(jsonObject.toString(), headers);
        return testRestTemplate.postForEntity("/api/account", accountAddRequest, String.class);
    }

    public ResponseEntity<String> editAccount(UUID id, HashMap<String, Object> accountMap) throws JsonProcessingException {
        String json = mapper.writeValueAsString(accountMap);
        HttpEntity<String> accountAddRequest = new HttpEntity<>(json, headers);
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/account/" + id, HttpMethod.PUT, accountAddRequest, String.class);
    }

    ResponseEntity<String> getAccount(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/account/" + id, String.class);
    }

    ResponseEntity<String> getAllAccounts() {
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/account", String.class);
    }

    public ResponseEntity<String> getAllAccountsFiltered(Map<String, String> filters) throws Exception {
        if(!filters.containsKey("login") || filters.size()>1){
            throw new Exception("not implemented");
        }

        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/account?login=" + filters.get("login"), String.class);
    }

    ResponseEntity<String> addReservable(Reservable reservable) throws JsonProcessingException {
        String jsonArray = mapper.writeValueAsString(reservable);
        HttpEntity<String> addReservableRequest = new HttpEntity<>(jsonArray, headers);
        return testRestTemplate.withBasicAuth(username, password).postForEntity("/api/reservable", addReservableRequest, String.class);
    }

    ResponseEntity<String> getAllReservableObjects(){
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/reservable", String.class);
    }

    ResponseEntity<String> addEvent(Event event) throws JsonProcessingException {
        String eventJson = mapper.writeValueAsString(event);
        HttpEntity<String> addEventRequest = new HttpEntity<>(eventJson, headers);
        return testRestTemplate.withBasicAuth(username, password).postForEntity("/api/event", addEventRequest, String.class);
    }

    ResponseEntity<String> getAllEvents(){
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/event", String.class);
    }

    ResponseEntity<String> getEventWithId(String eventId) {
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/event/" + eventId, String.class);
    }

    public ResponseEntity<String> editEvent(UUID id, HashMap<String, Object> updateMap) throws JsonProcessingException {
        String eventJson = mapper.writeValueAsString(updateMap);
        HttpEntity<String> editEventRequest = new HttpEntity<>(eventJson, headers);
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/event/" + id, HttpMethod.PUT, editEventRequest, String.class);
    }

    ResponseEntity<String> addReservation(Reservation reservation) throws JsonProcessingException {
        String reservationJsonObject = mapper.writeValueAsString(reservation);
        HttpEntity<String> addReservationRequest = new HttpEntity<>(reservationJsonObject, headers);
        return testRestTemplate.withBasicAuth(username, password).postForEntity("/api/reservation", addReservationRequest, String.class);
    }

    public ResponseEntity<String> deleteReservation(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/reservation/" + id,HttpMethod.DELETE, null, String.class);
    }


    public ResponseEntity<String> deleteReservable(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/reservable/" + id, HttpMethod.DELETE, null, String.class);
    }

    public ResponseEntity<String> deleteEvent(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/event/" + id, HttpMethod.DELETE, null, String.class);
    }

    public ResponseEntity<String> getReservableById(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/api/reservable/" + id, String.class);
    }

    public ResponseEntity<String> editReservable(UUID id, HashMap<String, Object> reservable) throws JsonProcessingException {
        String reservableJson = mapper.writeValueAsString(reservable);
        HttpEntity<String> editReservableResquest = new HttpEntity<>(reservableJson, headers);
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/reservable/" + id, HttpMethod.PUT, editReservableResquest, String.class);
    }

    public ResponseEntity<String> editReservation(UUID id, HashMap<String, Object> updateMap) throws JsonProcessingException {
        String reservationJson = mapper.writeValueAsString(updateMap);
        HttpEntity<String> editReservationResquest = new HttpEntity<>(reservationJson, headers);
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/reservation/" + id, HttpMethod.PUT, editReservationResquest, String.class);

    }

    public ResponseEntity<String> deleteAccount(UUID id) {
        return testRestTemplate.withBasicAuth(username, password).exchange("/api/account/" + id, HttpMethod.DELETE, null, String.class);
    }

    public ResponseEntity<String> generateToken() {
        return testRestTemplate.withBasicAuth(username, password).getForEntity("/authenticate", String.class);
    }
}
