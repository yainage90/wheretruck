package com.gamakdragons.wheretruck.foodtruck_region.service;

import java.io.IOException;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.model.RegionResponse;
import com.gamakdragons.wheretruck.util.ElasticSearchUtil;
import com.gamakdragons.wheretruck.util.EsRequestFactory;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
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
    public RegionResponse findAll() {

        SearchRequest request = EsRequestFactory.createSearchAllRequest(FOOD_TRUCK_REGION_INDEX_NAME); 

        SearchResponse searchResponse;
        try {
            searchResponse = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + searchResponse.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        RegionResponse response = ElasticSearchUtil.createRegionResponseFromSearchResponse(searchResponse);

        return response;
    }

    @Override
    public RegionResponse findByAddress(String city, String town) {

        SearchRequest request = EsRequestFactory.createAddressSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME ,city, town);
        SearchResponse searchResponse;
        try {
            searchResponse = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + searchResponse.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }
        
        RegionResponse response = ElasticSearchUtil.createRegionResponseFromSearchResponse(searchResponse);

        return response;
    }

    @Override
    public RegionResponse findByLocation(GeoLocation geoLocation, float distance) {

        SearchRequest request = EsRequestFactory.createGeoSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME, geoLocation, distance);
        SearchResponse searchResponse;
        try {
            searchResponse = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + searchResponse.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        RegionResponse response = ElasticSearchUtil.createRegionResponseFromSearchResponse(searchResponse);

        return response;
    }

}
