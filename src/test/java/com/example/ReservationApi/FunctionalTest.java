package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import com.example.ReservationApi.event.Event;
import com.example.ReservationApi.event.EventRepository;
import com.example.ReservationApi.reservation.Reservation;
import com.example.ReservationApi.reservation.ReservationRepository;
import com.example.ReservationApi.space.Seat;
import com.example.ReservationApi.space.SeatRepository;
import com.example.ReservationApi.space.Space;
import com.example.ReservationApi.space.SpaceRepository;
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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FunctionalTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	SpaceRepository spaceRepository;

	@Autowired
	SeatRepository seatRepository;

	@Autowired
	EventRepository eventRepository;

	@Autowired
	ReservationRepository reservationRepository;

	@Test
	void basicApiUsageTest() throws JSONException, JsonProcessingException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		//account add
		JSONObject accountJsonObject = new JSONObject();
		accountJsonObject.put("login", "user");
		accountJsonObject.put("password", "password");

		HttpEntity<String> accountAddRequest = new HttpEntity<>(accountJsonObject.toString(), headers);
		ResponseEntity<String> accountAddResponse = testRestTemplate.postForEntity("/api/account", accountAddRequest, String.class);
		Assert.assertEquals(200, accountAddResponse.getStatusCode().value());

		//get all events
		ResponseEntity<String> getEventsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/event", String.class);
		Assert.assertEquals(200, getEventsResponse.getStatusCodeValue());
		Assert.assertEquals("[]", getEventsResponse.getBody());

		//add event
		JSONObject eventJsonObject = new JSONObject();
		eventJsonObject.put("name", "myEvent");
		eventJsonObject.put("type", "one-to-one");
		eventJsonObject.put("numberOfSeats", 10);

		HttpEntity<String> addEventRequest = new HttpEntity<>(eventJsonObject.toString(), headers);
		ResponseEntity<String> addEventResponse = testRestTemplate.postForEntity("/api/event", addEventRequest, String.class);
		Assert.assertEquals(200, addEventResponse.getStatusCode().value());

		//get all events
		getEventsResponse = testRestTemplate.getForEntity("/api/event", String.class);
		Assert.assertEquals(200, getEventsResponse.getStatusCode().value());

		ObjectMapper objectMapper = new ObjectMapper();
		Event[] events = objectMapper.readValue(Objects.requireNonNull(getEventsResponse.getBody()), Event[].class);
		Assert.assertEquals( 1 , events.length);

		//get event created before
		UUID eventId = events[0].getId();
		ResponseEntity<String> getCreatedEventResponse = testRestTemplate.getForEntity("/api/account/" + eventId, String.class);
		Assert.assertEquals(200, getCreatedEventResponse.getStatusCodeValue());

		Event createdEvent = objectMapper.readValue(Objects.requireNonNull(getEventsResponse.getBody()), Event.class);
		Assert.assertEquals( eventId, createdEvent.getId());
		Assert.assertEquals( "myEvent", createdEvent.getName());
		//Assert.assertEquals( 10, createdEvent.getFreeSeats());



	}

}
