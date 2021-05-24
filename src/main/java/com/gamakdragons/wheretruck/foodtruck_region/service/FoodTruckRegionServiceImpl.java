package com.gamakdragons.wheretruck.foodtruck_region.service;

import java.io.IOException;
import java.util.List;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.util.ElasticSearchUtil;
import com.gamakdragons.wheretruck.foodtruck_region.util.SearchRequestFactory;

import org.apache.lucene.spatial3d.geom.GeoDistance;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FoodTruckRegionServiceImpl implements FoodTruckRegionService {
    
    @Value("${es.food_truck_region_index_name}")
    private String FOOD_TRUCK_REGION_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public FoodTruckRegionServiceImpl(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<FoodTruckRegion> findAll() {

        SearchRequest request = SearchRequestFactory.createMatchAllQuery(FOOD_TRUCK_REGION_INDEX_NAME); 

        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        List<FoodTruckRegion> regions = ElasticSearchUtil.getFoodTruckRegionFromResponse(response);
        log.info("total docs: " + regions.size());

        return regions;
    }

    @Override
    public List<FoodTruckRegion> findByAddress(String city) {
        return null;
    }

    @Override
    public List<FoodTruckRegion> findByLocation(GeoLocation geoLocation, float distance) {

        SearchRequest request = SearchRequestFactory.createGeoSearchQuery(FOOD_TRUCK_REGION_INDEX_NAME, geoLocation, distance);
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        List<FoodTruckRegion> regions = ElasticSearchUtil.getFoodTruckRegionFromResponse(response);
        log.info("total results: " + regions.size());

        return regions;
    }

}
