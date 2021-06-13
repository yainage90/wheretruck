package com.gamakdragons.wheretruck.domain.rating.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.domain.rating.service.RatingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(RatingController.class)
public class RatingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RatingService ratingService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	void testDelete() throws Exception {

		UpdateResultDto result = UpdateResultDto.builder().result("UPDATED").build();

		String truckId = UUID.randomUUID().toString();
		String ratingId = UUID.randomUUID().toString();

		given(ratingService.deleteRating(truckId, ratingId)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/rating/" + truckId + "/" + ratingId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testMy() throws Exception {

		String userId = UUID.randomUUID().toString();

		List<MyRatingDto> myRatings = new ArrayList<>();
		for(int i = 0; i < 5; i++) {
			MyRatingDto myRatingDto = MyRatingDto.builder()
						.truckId(UUID.randomUUID().toString())
						.truckName(UUID.randomUUID().toString().substring(0, 5))
						.id(UUID.randomUUID().toString())
						.userId(userId)
						.comment(UUID.randomUUID().toString())
						.createdDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
						.updatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
						.build();
			myRatings.add(myRatingDto);
		}

		SearchResultDto<MyRatingDto> result = SearchResultDto.<MyRatingDto> builder()
												.numFound(myRatings.size())
												.status("OK")
												.docs(myRatings)
												.build();

		given(ratingService.findByUserId(userId)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/rating/my")
														.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testSave() throws Exception {


		String truckId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		Rating rating = new Rating();
		rating.setStar(4.5f);
		rating.setComment("맛있어요");
		rating.setUserId(userId);

		UpdateResultDto result = UpdateResultDto.builder()
											.id(UUID.randomUUID().toString())
											.result("UPDATED")
											.build();

		given(ratingService.saveRating(truckId, rating)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/rating/" + truckId)
														.contentType(MediaType.APPLICATION_JSON)
														.content(objectMapper.writeValueAsString(rating))
														.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testUpdate() throws Exception {

		String truckId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		String ratingId = UUID.randomUUID().toString();

		Rating rating = new Rating();
		rating.setId(ratingId);
		rating.setStar(4.5f);
		rating.setComment("맛있어요");
		rating.setUserId(userId);

		UpdateResultDto result = UpdateResultDto.builder()
											.id(UUID.randomUUID().toString())
											.result("UPDATED")
											.build();

		given(ratingService.saveRating(truckId, rating)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/rating/" + truckId)
														.contentType(MediaType.APPLICATION_JSON)
														.content(objectMapper.writeValueAsString(rating))
														.requestAttr("userId", userId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}
}
