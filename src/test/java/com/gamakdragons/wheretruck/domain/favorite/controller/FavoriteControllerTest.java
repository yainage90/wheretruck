package com.gamakdragons.wheretruck.domain.favorite.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteService;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.google.gson.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FavoriteController.class)
public class FavoriteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FavoriteService favoriteService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	void testDelete() throws Exception {

		DeleteResultDto result = DeleteResultDto.builder().result("DELETED").build();

		String id = UUID.randomUUID().toString();
		given(favoriteService.deleteFavorite(id)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/favorite/" + id);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testCountByTruckId() throws Exception {

		String truckId = UUID.randomUUID().toString();

		given(favoriteService.countByTruckId(truckId)).willReturn(3);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/favorite/truck/" + truckId);
			
		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string("3"));
	}

	@Test
	void testMy() throws Exception {

		Truck truck = new Truck();
		truck.setId(UUID.randomUUID().toString());
		truck.setName("truckName");
		truck.setGeoLocation(new GeoLocation(35.0f, 135.0f));
		truck.setOpened(true);
		truck.setNumRating(10);
		truck.setStarAvg(4.5f);
		truck.setUserId(UUID.randomUUID().toString());
		truck.setImageUrl("http://foo-bar.com/abcd");

		SearchResultDto<Truck> result = SearchResultDto.<Truck> builder()
														.status("OK")
														.numFound(1)
														.docs(Collections.singletonList(truck))
														.build();

		given(favoriteService.findByUserId(truck.getUserId())).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/favorite/my")
																	.requestAttr("userId", truck.getUserId());
			
		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testSave() throws Exception {

		Favorite favorite = new Favorite();
		String userId = UUID.randomUUID().toString();
		String truckId = UUID.randomUUID().toString();
		favorite.setUserId(userId);
		favorite.setTruckId(truckId);

		IndexUpdateResultDto result = IndexUpdateResultDto.builder()
													.id(UUID.randomUUID().toString())
													.result("CREATED")
													.build();

		given(favoriteService.saveFavorite(favorite)).willReturn(result);

		JsonObject obj = new JsonObject();
		obj.addProperty("truckId", truckId);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/favorite")
																	.requestAttr("userId", userId)
																	.contentType(MediaType.APPLICATION_JSON)
																	.content(obj.toString());

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}
}
