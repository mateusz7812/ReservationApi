package com.example.reservationApi;

import com.example.reservationApi.account.Account;
import com.example.reservationApi.account.AccountService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JsonConvertTest {
    ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    public void accountJsonConverter() throws JsonProcessingException {
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account1 = accountService.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationService.save(new Reservation(account1, event, reservable));
        account1 = accountService.findById(account1.getId());

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
        accountService.save(new Account("user", "password"));
        Seat seat1 = new Seat("seat1");
        reservableService.save(seat1);
        seat1 = (Seat) reservableService.findAll().get(0);
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
        Reservable reservable = reservableService.save(new Seat("seat1"));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account1 = accountService.save(new Account("reservationTest", "password"));
        Reservation reservation = reservationService.save(new Reservation(account1, event, reservable));

        String reservationJsonString = objectMapper.writeValueAsString(reservation);
        Reservation reservationFromJson = objectMapper.readValue(reservationJsonString, Reservation.class);

        Assert.assertEquals(reservation.getId(), reservationFromJson.getId());
        Assert.assertEquals(reservation.getAccount().getId(), reservationFromJson.getAccount().getId());
        Assert.assertEquals(reservation.getEvent().getId(), reservationFromJson.getEvent().getId());
        Assert.assertEquals(reservation.getReservable().getId(), reservationFromJson.getReservable().getId());

    }

    @Test
    public void SeatJsonConverter() throws JsonProcessingException {
        Space space = (Space) reservableService.save(new Space("space1"));
        Reservable reservable = reservableService.save(new Seat("seat1", space));
        Event event = eventService.save(new Event(reservable, "event1"));
        Account account1 = accountService.save(new Account("reservationTest", "password"));
        reservationService.save(new Reservation(account1, event, reservable));
        reservable = reservableService.findById(reservable.getId());

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
        Space space = (Space) reservableService.save(new Space("space1"));
        reservableService.save(new Seat("seat1", space));
        space = (Space) reservableService.findById(space.getId());

        String spaceJsonString = objectMapper.writeValueAsString(space);
        Space spaceFromJson = (Space) objectMapper.readValue(spaceJsonString, Reservable.class);

        Assert.assertEquals(space.getId(), spaceFromJson.getId());
        Assert.assertNotNull(spaceFromJson.getReservables());
        List<Reservable> spaceReservableList = space.getReservables();
        List<Reservable> spaceFromJsonReservableList = spaceFromJson.getReservables();
        Assert.assertEquals(spaceReservableList.size(), spaceFromJsonReservableList.size());
        Assert.assertEquals(spaceReservableList.get(0).getId(), spaceFromJsonReservableList.get(0).getId());
    }

}
