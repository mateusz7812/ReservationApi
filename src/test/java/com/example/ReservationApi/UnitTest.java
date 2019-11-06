package com.example.ReservationApi;

import com.example.ReservationApi.account.Account;
import com.example.ReservationApi.account.AccountRepository;
import com.example.ReservationApi.event.EventRepository;
import com.example.ReservationApi.reservable.Reservable;
import com.example.ReservationApi.reservable.ReservableRepository;
import com.example.ReservationApi.reservation.ReservationRepository;
import com.example.ReservationApi.reservable.types.Seat;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UnitTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    private ReservableRepository reservableRepository;

    @Before
    public void before(){
        TestMethods.setTestRestTemplate(testRestTemplate);
    }

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
    public void addAccountTest() throws JSONException {
        TestMethods.addAccount("user", "password");

        List<Account> accounts = accountRepository.findAll();
        Assert.assertEquals(1, accounts.size());
        Account account = accounts.get(0);
        Assert.assertEquals("user", account.getLogin());
        Assert.assertTrue(account.checkPassword("password"));
    }

    @Test
    public void inheritedEntityRepositoryTest(){
        reservableRepository.save(new Seat("A1"));
        Assert.assertEquals(1, reservableRepository.findAll().size());
        Assert.assertEquals(Seat.class, reservableRepository.findAll().get(0).getClass());
    }

    @Test
    public void addSeatTest() throws JSONException {
        TestMethods.addAccount("user", "password");
        TestMethods.addOneSeat("user", "password");

        Assert.assertEquals(1, reservableRepository.findAll().size());
        Reservable reservable = reservableRepository.findAll().get(0);
        Assert.assertEquals(Seat.class, reservable.getClass());
    }
}
