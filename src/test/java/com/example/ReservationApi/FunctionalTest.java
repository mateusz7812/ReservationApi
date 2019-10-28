package com.example.ReservationApi;

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

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class FunctionalTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	void accountAdd() throws JSONException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject accountJsonObject = new JSONObject();
		accountJsonObject.put("login", "fakeLogin");

		HttpEntity<String> request = new HttpEntity<String>(accountJsonObject.toString(), headers);
		ResponseEntity<String> responseEntityStr = testRestTemplate.postForEntity("http://localhost:" + port + "/api/account", request, String.class);
		Assert.assertEquals(200, responseEntityStr.getStatusCode().value());
	}

	@Test
	void eventAdd() throws JSONException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		JSONObject eventJsonObject = new JSONObject();
		eventJsonObject.put("accountId", UUID.randomUUID());
		eventJsonObject.put("name", "test");
		eventJsonObject.put("type", "test");

		HttpEntity<String> request = new HttpEntity<String>(eventJsonObject.toString(), headers);
		ResponseEntity<String> responseEntityStr = testRestTemplate.postForEntity("http://localhost:" + port +"/api/event", request, String.class);
		Assert.assertEquals(200, responseEntityStr.getStatusCode().value());
	}

}
