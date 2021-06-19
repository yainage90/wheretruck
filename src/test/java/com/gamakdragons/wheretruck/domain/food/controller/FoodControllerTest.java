package com.gamakdragons.wheretruck.domain.food.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.service.FoodService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FoodController.class)
public class FoodControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FoodService foodService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	void testDelete() throws Exception {

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").build();

		String truckId = UUID.randomUUID().toString();
		String foodId = UUID.randomUUID().toString();

		given(foodService.deleteFood(truckId, foodId)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/food/" + truckId + "/" + foodId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testSave() throws Exception {
		
		String truckId = UUID.randomUUID().toString();
		String foodId = UUID.randomUUID().toString();

		byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", "foodImage.png", MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

		FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
		foodSaveRequestDto.setName("팥빵");
		foodSaveRequestDto.setCost(1000);
		foodSaveRequestDto.setDescription("맛있어요");
		foodSaveRequestDto.setImage(image);

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").id(foodId).build();

		given(foodService.saveFood(truckId, foodSaveRequestDto)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/food/" + truckId)
																	.file(image)
																	.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
																	.param("name", foodSaveRequestDto.getName())
																	.param("cost", String.valueOf(foodSaveRequestDto.getCost()))
																	.param("description", foodSaveRequestDto.getDescription());

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testUpdate() throws Exception {
		
		String truckId = UUID.randomUUID().toString();
		String foodId = UUID.randomUUID().toString();

		byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", "foodImage.png", MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

		FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
		foodSaveRequestDto.setId(foodId);
		foodSaveRequestDto.setName("팥빵");
		foodSaveRequestDto.setCost(1000);
		foodSaveRequestDto.setDescription("맛있어요");
		foodSaveRequestDto.setImage(image);

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").id(foodId).build();

		given(foodService.updateFood(truckId, foodSaveRequestDto)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/food/" + truckId)
																	.file(image)
																	.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
																	.param("id", foodId)
																	.param("name", foodSaveRequestDto.getName())
																	.param("cost", String.valueOf(foodSaveRequestDto.getCost()))
																	.param("description", foodSaveRequestDto.getDescription());

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testSort() throws Exception {

		String truckId = UUID.randomUUID().toString();
		List<String> ids = new ArrayList<>();
		for(int i = 0; i < 5; i++) {
			ids.add(UUID.randomUUID().toString());
		}

		IndexUpdateResultDto result = IndexUpdateResultDto.builder().result("UPDATED").build();

		given(foodService.sortFoods(truckId, ids)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/food/" + truckId + "/sort/" + String.join(",", ids));

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

}
