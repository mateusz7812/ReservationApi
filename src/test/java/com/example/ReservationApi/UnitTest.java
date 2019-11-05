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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UnitTest {

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
    public void testAuthorization(){
        ResponseEntity<String> notAuthorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(401, notAuthorizedGetAccountsResponse.getStatusCodeValue());

        Account account = new Account("user", "password");
        accountRepository.save(account);

        ResponseEntity<String> authorizedGetAccountsResponse = testRestTemplate.withBasicAuth("user", "password").getForEntity("/api/account", String.class);
        Assert.assertEquals(200, authorizedGetAccountsResponse.getStatusCodeValue());

    }

    @Test
    public void testMakeReservation(){
        Account account = new Account("login", "password");
        accountRepository.save(account);

        Space space = new Space();
        spaceRepository.save(space);

        for(int i=0; i<10; i++){
            seatRepository.save(new Seat(space));
        }

        Event event = new Event(UUID.randomUUID(), account, space, "event");
        eventRepository.save(event);
        event = eventRepository.findAll().get(0);

        Assert.assertEquals(10, event.getFreeSeats().length);

        Reservation reservation = new Reservation(UUID.randomUUID(), event, event.getFreeSeats()[0]);
        reservationRepository.save(reservation);

        event = eventRepository.findAll().get(0);
        Assert.assertEquals(9, event.getFreeSeats().length);

    }
}
