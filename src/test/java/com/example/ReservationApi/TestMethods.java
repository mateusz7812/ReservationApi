package com.example.ReservationApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

class TestMethods {
    private static TestRestTemplate testRestTemplate;

    private static HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    static void addAccount(String login, String password) throws JSONException {
        JSONObject accountJsonObject = new JSONObject();
        accountJsonObject.put("login", login);
        accountJsonObject.put("password", password);

        HttpEntity<String> accountAddRequest = new HttpEntity<>(accountJsonObject.toString(), headers());
        ResponseEntity<String> accountAddResponse = testRestTemplate.postForEntity("/api/account", accountAddRequest, String.class);
        Assert.assertEquals(200, accountAddResponse.getStatusCode().value());
    }

    static void setTestRestTemplate(TestRestTemplate testRestTemplate) {
        TestMethods.testRestTemplate = testRestTemplate;
    }

    static void addSpace(String name, String username, String password) throws JSONException {
        JSONObject spaceJsonObject = new JSONObject();
        spaceJsonObject.put("name", name);

        HttpEntity<String> addSpaceRequest = new HttpEntity<>(spaceJsonObject.toString(), headers());
        ResponseEntity<String> addSpaceResponse = testRestTemplate.withBasicAuth(username, password).postForEntity("/api/space", addSpaceRequest, String.class);
        Assert.assertEquals(200, addSpaceResponse.getStatusCode().value());
    }

    public static void addOneSeat(String username, String password) throws JSONException {
        JSONObject seat = new JSONObject();
        seat.put("type", "Seat");
        seat.put("row", "A");
        seat.put("column", "1");

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(seat);

        HttpEntity<String> addSeatsRequest = new HttpEntity<>(jsonArray.toString(), headers());
        ResponseEntity<String> addSeatsResponse = testRestTemplate.withBasicAuth(username, password).postForEntity("/api/reservable", addSeatsRequest, String.class);
        Assert.assertEquals(200, addSeatsResponse.getStatusCode().value());

    }
}
