package springmvc.unittest.test;

import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import springmvc.unittest.model.User;


/**
 * Class to test REST client code
 * */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:web.xml", "classpath*:config/applicationContext.xml",
		"classpath*:spring-servlet.xml" })
public class UserControllerIntegrationTest {
	private static final int UNKNOWN_ID = Integer.MAX_VALUE;
	private static final String BASE_URI = "http://localhost:8080/mockitoWithSpringMVC/users";

	private RestTemplate template;

	public UserControllerIntegrationTest() {
		template = new RestTemplate();
		template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		template.getMessageConverters().add(new StringHttpMessageConverter());
	}
	// ==================== TEST ======================

	@Test
	@Ignore
	public void test_get_all_success() {
		ResponseEntity<User[]> response = template.getForEntity(BASE_URI, User[].class);
		System.out.println("result: " + response.toString());
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		// validateCORSHttpHeaders(response.getHeaders());
	}

	// ==================== Get User By ID ===============

	@Test
	public void test_get_by_id_success() {

		ResponseEntity<User> response = template.getForEntity(BASE_URI + "/1", User.class);
		User user = response.getBody();

		assertThat(user.getId(), is(1));
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		System.out.println("result: " + user.toString());
	}

	@Test
	@Ignore
	public void test_get_by_id_failure_not_found() {
		try {
			template.getForEntity(BASE_URI + "/" + UNKNOWN_ID, User.class);
			fail("should return 404 not found");
		} catch (HttpClientErrorException e) {
			System.out.println("Message error: " + e.getMessage());
			assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
		}
	}

	// ======================== Create New User =========================

	@Test
	public void test_create_new_user_success() {
		User newUser = new User("new username_" + Math.random());
		URI location = template.postForLocation(BASE_URI, newUser, User.class);
		assertThat(location, notNullValue());
	}

	@Test
	public void test_create_new_user_fail_exists() {
		User existingUser = new User("VanVTT");
		try {
			template.postForLocation(BASE_URI, existingUser, User.class);
			fail("should return 409 conflict");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode(), is(HttpStatus.CONFLICT));
		}
	}

	@Test
	public void test_update_user_success() {

		// check result
		System.out.println("before update user = " + template.getForEntity(BASE_URI + "/2", User.class).getBody());

		User existingUser = new User(2, " Test Updated");
		template.put(BASE_URI + "/" + existingUser.getId(), existingUser);

		// check result
		ResponseEntity<User> response = template.getForEntity(BASE_URI + "/2", User.class);
		System.out.println("user = " + response.getBody());
	}

	@Test
	@Ignore
	public void test_update_user_fail() {
		User existingUser = new User(UNKNOWN_ID, "update");
		try {
			template.put(BASE_URI + "/" + existingUser.getId(), existingUser);
			fail("should return 404 not found");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
		}
	}

	// ================== Delete User ==================

	@Test
	@Ignore
	public void test_delete_user_success() {
		template.delete(BASE_URI + "/" + getLastUser().getId());
	}

	@Test
	@Ignore
	public void test_delete_user_fail() {
		try {
			template.delete(BASE_URI + "/" + UNKNOWN_ID);
			fail("should return 404 not found");
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode(), is(HttpStatus.NOT_FOUND));
		}
	}

	private User getLastUser() {
		ResponseEntity<User[]> response = template.getForEntity(BASE_URI, User[].class);
		User[] users = response.getBody();
		return users[users.length - 1];
	}
}
