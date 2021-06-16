package com.gamakdragons.wheretruck.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.dto.Role;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.domain.user.service.UserService;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = Jackson2ObjectMapperBuilder.json().build();
	}

	@Test
	void testGetById() throws Exception {

		String userId = UUID.randomUUID().toString();

		User user = new User();
		user.setId(userId);
		user.setNickName("yain");
		user.setRole(Role.OWNER);

		given(userService.getById(userId)).willReturn(user);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/user/me")
															.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(user)));
	}

	@Test
	void testUpdateNickName() throws Exception {

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").build();

		String userId = UUID.randomUUID().toString();
		String nickName = UUID.randomUUID().toString().substring(0, 6);

		JsonObject obj = new JsonObject();
		obj.addProperty("nickName", nickName);

		given(userService.changeNickName(userId, nickName)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/user/nickname")
																	.contentType(MediaType.APPLICATION_JSON)
																	.content(obj.toString())
																	.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testUpdateRole() throws Exception {

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").build();

		String userId = UUID.randomUUID().toString();
		Role role = Role.USER;

		JsonObject obj = new JsonObject();
		obj.addProperty("role", role.name());

		given(userService.changeRole(userId, role)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/user/role")
																	.contentType(MediaType.APPLICATION_JSON)
																	.content(obj.toString())
																	.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}
}
