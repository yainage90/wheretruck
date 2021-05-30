package com.gamakdragons.wheretruck.foodtruck_region.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FoodTruckRegionServiceImpl implements FoodTruckRegionService {
    
    @Value("${es.index.food_truck_region.name}")
    private String FOOD_TRUCK_REGION_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public FoodTruckRegionServiceImpl(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public SearchResultDto<FoodTruckRegion> findAll() {

        SearchRequest request = EsRequestFactory.createSearchAllRequest(FOOD_TRUCK_REGION_INDEX_NAME); 

        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);
    }

    @Override
    public SearchResultDto<FoodTruckRegion> findByAddress(String city, String town) {

        SearchRequest request = EsRequestFactory.createAddressSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME ,city, town);
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);
    }

    @Override
    public SearchResultDto<FoodTruckRegion> findByLocation(GeoLocation geoLocation, float distance) {

        SearchRequest request = EsRequestFactory.createGeoSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME, geoLocation, distance);
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }


        return makeSearhResultDtoFromSearchResponse(response);
    }

    private SearchResultDto<FoodTruckRegion> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<FoodTruckRegion> builder()
                .status(response.status().name())
                .numFound(response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), FoodTruckRegion.class))
                            .collect(Collectors.toList())
                ).build();

    }

    private SearchResultDto<FoodTruckRegion> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<FoodTruckRegion> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }
}
