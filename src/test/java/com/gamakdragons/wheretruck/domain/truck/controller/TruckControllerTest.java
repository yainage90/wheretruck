package com.gamakdragons.wheretruck.domain.truck.controller;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.domain.truck.service.TruckService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@WebMvcTest(TruckController.class)
public class TruckControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TruckService truckService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = Jackson2ObjectMapperBuilder.json().build();
	}

	@Test
	void testDelete() throws Exception {

		DeleteResultDto result = DeleteResultDto.builder().result("DELETED").build();

		given(truckService.deleteTruck(anyString())).willReturn(result);

		mockMvc.perform(delete("/api/truck/" + UUID.randomUUID().toString()))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testGetAllTrucks() throws Exception {

		SearchResultDto<Truck> result = createTruckSearchResultDto();

		given(truckService.findAll()).willReturn(result);

		mockMvc.perform(get("/api/truck/all"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testGetByGeoLocation() throws Exception {

		SearchResultDto<Truck> result = createTruckSearchResultDto();

		given(truckService.findByGeoLocation(isA(GeoLocation.class), anyFloat())).willReturn(result);

		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		requestParams.set("lat", String.valueOf(30.0f));
		requestParams.set("lon", String.valueOf(130.0f));
		requestParams.set("distance", String.valueOf(10.0f));

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/truck/geo").params(requestParams);

		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testGetById() throws Exception {

		Truck truck = new Truck(
            UUID.randomUUID().toString(), //id
            "truck1", //name
            new GeoLocation(30.0f, 130.0f), //geoLocation
            "this is truck1", //description
            false, //opened
            UUID.randomUUID().toString(), //userId
            0, //numRating
            0.0f, //starAvg
			null,
            null, //foods
            null //ratings
        );

		given(truckService.getById(truck.getId())).willReturn(truck);
			
		mockMvc.perform(get("/api/truck/" + truck.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(truck)));
	}

	@Test
	void testGetByIds() throws Exception {

		SearchResultDto<Truck> result = createTruckSearchResultDto();

		List<String> ids = result.getDocs().stream().map(truck -> truck.getId()).collect(Collectors.toList());
		given(truckService.getByIds(ids)).willReturn(result);

		mockMvc.perform(get("/api/truck/favorite/" + String.join(",", ids)))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testGetByUserId() throws Exception {

		Truck truck = new Truck(
            UUID.randomUUID().toString(), //id
            "truck1", //name
            new GeoLocation(30.0f, 130.0f), //geoLocation
            "this is truck1", //description
            false, //opened
            UUID.randomUUID().toString(), //userId
            0, //numRating
            0.0f, //starAvg
			null,
            null, //foods
            null //ratings
        );

		SearchResultDto<Truck> result = SearchResultDto.<Truck> builder()
														.status("OK")
														.numFound(1)
														.docs(Collections.singletonList(truck))
														.build();

		given(truckService.findByUserId(truck.getUserId())).willReturn(result);

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/truck/my")
															.requestAttr("userId", truck.getUserId());
			
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testSave() throws Exception {

		byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", "truckImage.png", MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

		String userId = UUID.randomUUID().toString();

		TruckSaveRequestDto dto = new TruckSaveRequestDto();
		dto.setName("truck1");
		dto.setDescription("this is truck1");
		dto.setImage(image);
		dto.setUserId(userId);

		IndexUpdateResultDto result = IndexUpdateResultDto.builder()
													.id(UUID.randomUUID().toString())
													.result("CREATED")
													.build();

		given(truckService.saveTruck(dto)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/truck")
																			.file(image)
																			.contentType(MediaType.MULTIPART_FORM_DATA)
																			.param("name", dto.getName())
																			.param("description", dto.getDescription())
																			.requestAttr("userId", userId);
																			

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testStartTruck() throws Exception {

		String truckId = UUID.randomUUID().toString();

		IndexUpdateResultDto result = IndexUpdateResultDto.builder()
												.result("UPDATED")
												.id(truckId)
												.build();

		given(truckService.openTruck(anyString(), isA(GeoLocation.class))).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/truck/start/" + truckId)
																		.content(objectMapper.writeValueAsString(new GeoLocation(35.0f, 135.0f)))
																		.contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testStopTruck() throws Exception {

		String truckId = UUID.randomUUID().toString();

		IndexUpdateResultDto result = IndexUpdateResultDto.builder()
												.result("UPDATED")
												.id(truckId)
												.build();

		given(truckService.stopTruck(truckId)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/truck/stop/" + truckId);

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	void testUpdate() throws Exception {

		byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", "truckImage.png", MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

		String truckId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();

		TruckSaveRequestDto dto = new TruckSaveRequestDto();
		dto.setId(truckId);
		dto.setName("truck1 updated");
		dto.setDescription("this is truck1 updated");
		dto.setImage(image);
		dto.setUserId(userId);

		IndexUpdateResultDto result = IndexUpdateResultDto.builder()
													.id(UUID.randomUUID().toString())
													.result("UPDATED")
													.build();

		given(truckService.updateTruck(dto)).willReturn(result);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart("/api/truck")
																			.file(image)
																			.contentType(MediaType.MULTIPART_FORM_DATA)
																			.param("id", truckId)
																			.param("name", dto.getName())
																			.param("description", dto.getDescription())
																			.requestAttr("userId", userId);
																			

		mockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	private SearchResultDto<Truck> createTruckSearchResultDto() {

		List<Truck> trucks = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			trucks.add(
				new Truck(
            		UUID.randomUUID().toString(), //id
            		"truck1", //name
            		new GeoLocation(30.0f + i, 130.0f + i), //geoLocation
            		"this is truck1", //description
            		false, //opened
            		UUID.randomUUID().toString(), //userId
            		0, //numRating
            		0.0f, //starAvg
					null,
            		null, //foods
            		null //ratings
        		)
			);
		}

		SearchResultDto<Truck> result = SearchResultDto.<Truck> builder()
											.status("OK")
											.numFound(10)
											.docs(trucks)
											.build();
		
		return result;
	}
}
