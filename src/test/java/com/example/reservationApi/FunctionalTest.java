package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.event.Event;
import com.example.reservationApi.reservable.Reservable;
import com.example.reservationApi.reservable.types.Seat;
import com.example.reservationApi.reservable.types.Space;
import com.example.reservationApi.reservation.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FunctionalTest {

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	void oneUserApiTest() throws IOException {

		Account account = new Account("user", "password");
		String password = "password";

		TestMethods testMethods = new TestMethods(account.getLogin(), password, testRestTemplate);

		//add a account
		ResponseEntity<String> addAccountResponse = testMethods.addAccount(account);
		account = mapper.readValue(Objects.requireNonNull(addAccountResponse.getBody()), Account.class);

		//add one seat
		ResponseEntity<String> addSeatResponse = testMethods.addReservable(new Seat("seat"));
		Seat seat = mapper.readValue(Objects.requireNonNull(addSeatResponse.getBody()), Seat.class);

		//add a event
		ResponseEntity<String> getEventResponse = testMethods.addEvent(new Event(seat, "event"));
		Event event = mapper.readValue(Objects.requireNonNull(getEventResponse.getBody()), Event.class);

		//add reservation
		testMethods.addReservation(new Reservation(account, event, seat));

		//check reservation
		ResponseEntity<String> getAccountResponse = testMethods.getAccount(account.getId());
		account = mapper.readValue(Objects.requireNonNull(getAccountResponse.getBody()), Account.class);
		List<Reservation> reservations = account.getReservations();
		Assert.assertEquals(1, reservations.size());
		Reservation reservation = reservations.get(0);
		Assert.assertEquals(account.getId(), reservation.getAccount().getId());
		Assert.assertEquals(event.getId(), reservation.getEvent().getId());
		Assert.assertEquals(seat.getId(), reservation.getReservable().getId());

	}

	@Test
	void moreUsersApiTest() throws JsonProcessingException {
		TestMethods testMethods = new TestMethods(testRestTemplate);

		//add accounts
		Account user1 = new Account("user", "password");
		Account user2 = new Account("user2", "password");

		ResponseEntity<String> addUser1Response = testMethods.addAccount(user1);
		user1 = mapper.readValue(Objects.requireNonNull(addUser1Response.getBody()), Account.class);

		ResponseEntity<String> addUser2Response = testMethods.addAccount(user2);
		user2 = mapper.readValue(Objects.requireNonNull(addUser2Response.getBody()), Account.class);

		//edit account
		user1.setLogin("user1");
		ResponseEntity<String> updateUser1Response = testMethods.editAccount(user1.getId(), user1);
		user1 = mapper.readValue(Objects.requireNonNull(updateUser1Response.getBody()), Account.class);

		user2.setPassword("other");
		ResponseEntity<String> updateUser2Response = testMethods.editAccount(user2.getId(), user2);
		user2 = mapper.readValue(Objects.requireNonNull(updateUser2Response.getBody()), Account.class);

		//add reservable objects
		ResponseEntity<String> addSpace1Response = testMethods.addReservable(new Space("space1"));
		Space space1 = mapper.readValue(Objects.requireNonNull(addSpace1Response.getBody()), Space.class);

		ResponseEntity<String> addSpace2Response = testMethods.addReservable(new Space("space2"));
		Space space2 = mapper.readValue(Objects.requireNonNull(addSpace2Response.getBody()), Space.class);

		ResponseEntity<String> addSpace3Response = testMethods.addReservable(new Space("space3", space1));
		Space space3 = mapper.readValue(Objects.requireNonNull(addSpace3Response.getBody()), Space.class);

		ResponseEntity<String> addSpace4Response = testMethods.addReservable(new Space("space4", space1));
		Space space4 = mapper.readValue(Objects.requireNonNull(addSpace4Response.getBody()), Space.class);

		testMethods.addReservable(new Seat("seat1", space1));
		testMethods.addReservable(new Seat("seat2", space2));
		testMethods.addReservable(new Seat("seat3", space2));
		testMethods.addReservable(new Seat("seat4", space3));
		testMethods.addReservable(new Seat("seat5", space3));
		testMethods.addReservable(new Seat("seat6", space3));
		testMethods.addReservable(new Seat("seat7", space3));
		testMethods.addReservable(new Seat("seat8", space4));
		testMethods.addReservable(new Seat("seat9", space4));
		ResponseEntity<String> response = testMethods.setPass("user1", "password").getAllReservableObjects();
		List<Reservable> reservables = mapper.readValue(Objects.requireNonNull(response.getBody()), new TypeReference<>() {});

		//add events
		ResponseEntity<String> addEvent1Response = testMethods.addEvent(new Event(space1, "event1", 1200, 1450));
		Event event1 = mapper.readValue(Objects.requireNonNull(addEvent1Response.getBody()), Event.class);

		ResponseEntity<String> addEvent2Response = testMethods.addEvent(new Event(space2, "event2", 1300, 1400));
		Event event2 = mapper.readValue(Objects.requireNonNull(addEvent2Response.getBody()), Event.class);

		ResponseEntity<String> addEvent3Response = testMethods.addEvent(new Event(space4, "event3", 1500, 1800));
		Event event3 = mapper.readValue(Objects.requireNonNull(addEvent3Response.getBody()), Event.class);

		//add reservation
		Reservation reservation1 = new Reservation(user1, event3, reservables.get(7));
		testMethods.addReservation(reservation1);

		Reservation reservation2 = new Reservation(user1, event3, reservables.get(12));
		testMethods.addReservation(reservation2);

		Reservation reservation3 = new Reservation(user2, event1, reservables.get(10));
		testMethods.setPass("user2", "other").addReservation(reservation3);

		Reservation reservation4 = new Reservation(user2, event2, space2);
		testMethods.addReservation(reservation4);

		//delete reservation
		testMethods.deleteReservation(reservation3.getId());

		//edit reservation
		reservation1.setReservable(reservables.get(8));
		testMethods.editReservation(reservation1.getId(), reservation1);

		//edit event
		event1.setReservable(space3);
		testMethods.editEvent(event1.getId(), event1);

		//delete reservable
		testMethods.deleteReservable(space1.getId());

		//delete event
		testMethods.deleteEvent(event2.getId());

	}
}
