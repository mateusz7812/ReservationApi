package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FunctionalTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	void basicApiTest() throws JSONException, JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//add a account
		TestMethods.setTestRestTemplate(testRestTemplate);
		TestMethods.addAccount("user", "password");

		//get all accounts
		ResponseEntity<String> getAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
		Assert.assertEquals(200, getAccountsResponse.getStatusCodeValue());
		Account[] accountsArray = mapper.readValue(Objects.requireNonNull(getAccountsResponse.getBody()), Account[].class);
		Assert.assertEquals(1, accountsArray.length);

		UUID accountId = accountsArray[0].getId();

		//add one seat to the space
		TestMethods.addOneSeat("user", "password");

		//add a event
		JSONObject eventJsonObject = new JSONObject();
		eventJsonObject.put("name", "myEvent");
		eventJsonObject.put("account", accountId);

		HttpEntity<String> addEventRequest = new HttpEntity<>(eventJsonObject.toString(), headers);
		ResponseEntity<String> addEventResponse = testRestTemplate.withBasicAuth("user", "password").postForEntity("/api/event", addEventRequest, String.class);
		Assert.assertEquals(200, addEventResponse.getStatusCode().value());

		//get all events
		ResponseEntity<String> getEventsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/event", String.class);
		Assert.assertEquals(200, getEventsResponse.getStatusCode().value());
		Event[] events = mapper.readValue(Objects.requireNonNull(getEventsResponse.getBody()), Event[].class);
		Assert.assertEquals( 1 , events.length);

		//get event created before
		UUID eventId = events[0].getId();
		ResponseEntity<String> getCreatedEventResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account/" + eventId, String.class);
		Assert.assertEquals(200, getCreatedEventResponse.getStatusCodeValue());

		Event createdEvent = mapper.readValue(Objects.requireNonNull(getEventsResponse.getBody()), Event.class);
		Assert.assertEquals( eventId, createdEvent.getId());
		Assert.assertEquals( "myEvent", createdEvent.getName());
		//Assert.assertEquals( 10, createdEvent.getFreeSeats());



	}

}
