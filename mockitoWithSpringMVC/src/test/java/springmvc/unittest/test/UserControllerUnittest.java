package springmvc.unittest.test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import springmvc.unittest.Filter.CORSFilter;
import springmvc.unittest.controller.UserController;
import springmvc.unittest.model.User;
import springmvc.unittest.service.UserService;

/**
 * Class to test REST API code
 * */

public class UserControllerUnittest {

	/**
	 * Prepare
	 * */
	private MockMvc mockMVC;

	@Mock
	private UserService userService;

	@InjectMocks
	private UserController userController;

	/**
	 * Require
	 * */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		mockMVC = MockMvcBuilders
				.standaloneSetup(userController)
				.addFilters (new CORSFilter())
				.build();
	}
	
	/**
	 * Test method GET
	 * */
	@Test
	public void unittest_get_all_users_return_success() throws Exception{
		List<User> listUser = Arrays.asList(
				new User (1, "vanvtt"),
				new User (2, "ThaoDTD"));
		
		when (userService.getAll()).thenReturn(listUser);
		
		mockMVC.perform(get("/users"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("vanvtt")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].name", is("ThaoDTD")));

    verify(userService, times(1)).getAll();
    verifyNoMoreInteractions(userService);
		
	}
	
	@Test
	public void test_get_by_id_success() throws Exception {
        User user = new User(1, "Van Vu");

        when(userService.findById(1)).thenReturn(user);

        mockMVC.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Van Vu")));

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }
	@Test
	public void test_get_by_id_fail_404_not_found() throws Exception {
        when(userService.findById(1)).thenReturn(null);

        mockMVC.perform(get("/users/{id}", 1))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(1);
        verifyNoMoreInteractions(userService);
    }
	
	/**
	 * Test POST method
	 * */
	@Test
    public void test_create_user_success() throws Exception {
        User user = new User("Van Vu");

        when(userService.exists(user)).thenReturn(false);
        doNothing().when(userService).create(user);

        mockMVC.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", containsString("/users/0")));

        verify(userService, times(1)).exists(user);
        verify(userService, times(1)).create(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_create_user_fail_404_not_found() throws Exception {
        User user = new User("username exists");

        when(userService.exists(user)).thenReturn(true);
        mockMVC.perform(
                post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).exists(user);
        verifyNoMoreInteractions(userService);
    }
    
    
    /**
     * Test PUT method
     * */
    @Test
    public void test_update_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).update(user);

        mockMVC.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).update(user);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_update_user_fail_404_not_found() throws Exception {
        User user = new User(999, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMVC.perform(
                put("/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }

    /**
     * Test Delete method
     * */
    @Test
    public void test_delete_user_success() throws Exception {
        User user = new User(1, "Arya Stark");

        when(userService.findById(user.getId())).thenReturn(user);
        doNothing().when(userService).delete(user.getId());

        mockMVC.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1)).findById(user.getId());
        verify(userService, times(1)).delete(user.getId());
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void test_delete_user_fail_404_not_found() throws Exception {
        User user = new User(999, "user not found");

        when(userService.findById(user.getId())).thenReturn(null);

        mockMVC.perform(
                delete("/users/{id}", user.getId()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(user.getId());
        verifyNoMoreInteractions(userService);
    }
    
    /**
     * Test CROS header
     * */
    @Test
    public void test_cors_headers() throws Exception {
        mockMVC.perform(get("/users"))
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().string("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE"))
                .andExpect(header().string("Access-Control-Allow-Headers", "*"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"));
    }
    
    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
