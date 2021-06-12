package com.gamakdragons.wheretruck.domain.region.controller;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.region.entity.Region;
import com.gamakdragons.wheretruck.domain.region.service.RegionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@WebMvcTest(RegionController.class)
public class RegionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RegionService regionService;

	private ObjectMapper objectMapper;

	@BeforeEach
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@DisplayName("전체 허가구역")
	void testAllRegions() throws Exception {

		//given
		List<Region> regions = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			regions.add(Region.builder()
								.regionName(UUID.randomUUID().toString().substring(0, 6))
								.regionType(new Random().nextInt(5))
								.city(UUID.randomUUID().toString().substring(0, 5))
								.town(UUID.randomUUID().toString().substring(0, 5))
								.build()
			);
		}

		SearchResultDto<Region> result = SearchResultDto.<Region> builder()
											.status("OK")
											.numFound(10)
											.docs(regions)
											.build();

		given(regionService.findAll()).willReturn(result);

		mockMvc.perform(get("/api/region/all"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}

	@Test
	@DisplayName("위치기반 검색")
	void testGetRegionsByAddress() throws Exception {

		//given
		List<Region> regions = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			regions.add(Region.builder()
								.regionName(UUID.randomUUID().toString().substring(0, 6))
								.geoLocation(new GeoLocation(30.0f + i, 130.0f + i))
								.regionType(new Random().nextInt(5))
								.city(UUID.randomUUID().toString().substring(0, 5))
								.town(UUID.randomUUID().toString().substring(0, 5))
								.build()
			);
		}

		SearchResultDto<Region> result = SearchResultDto.<Region> builder()
													.status("OK")
													.numFound(10)
													.docs(regions)
													.build();
		
		given(regionService.findByLocation(isA(GeoLocation.class), anyFloat())).willReturn(result);
		
		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		requestParams.set("lat", String.valueOf(30.0f));
		requestParams.set("lon", String.valueOf(130.0f));
		requestParams.set("distance", String.valueOf(10.0f));

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/region/geo").params(requestParams);

		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)))
				.andDo(print());
	}

	@Test
	@DisplayName("주소기반 검색")
	void testGetRegionsByDistance() throws Exception {

		List<Region> regions = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			regions.add(Region.builder()
								.regionName(UUID.randomUUID().toString().substring(0, 6))
								.geoLocation(new GeoLocation(30.0f + i, 130.0f + i))
								.regionType(new Random().nextInt(5))
								.city(UUID.randomUUID().toString().substring(0, 5))
								.town(UUID.randomUUID().toString().substring(0, 5))
								.build()
			);
		}

		SearchResultDto<Region> result = SearchResultDto.<Region> builder()
													.status("OK")
													.numFound(10)
													.docs(regions)
													.build();

		String city = UUID.randomUUID().toString().substring(0, 5);
		String town = UUID.randomUUID().toString().substring(0, 5);

		given(regionService.findByAddress(anyString(), anyString())).willReturn(result);

		MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
		requestParams.set("city", city);
		requestParams.set("town", town);

		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/region/address").params(requestParams);

		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/json"))
				.andExpect(content().string(objectMapper.writeValueAsString(result)));
	}
}
