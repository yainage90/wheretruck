package com.gamakdragons.wheretruck.foodtruck_region.service;

import java.io.IOException;
import java.util.List;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.util.ElasticSearchUtil;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FoodTruckRegionService {
    
    @Value("${es.food_truck_region_index_name}")
    private String FOOD_TRUCK_REGION_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public FoodTruckRegionService(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    public List<FoodTruckRegion> findAll() {

        SearchRequest request = new SearchRequest(FOOD_TRUCK_REGION_INDEX_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        request.source(searchSourceBuilder);

        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            return null;
        }

        List<FoodTruckRegion> items = ElasticSearchUtil.getFoodTruckRegionFromResponse(response);
        log.info("total docs: " + items.size());

        return items;
    }

    public SearchResponse findByAddress(String city) {
        return null;
    }

    public SearchResponse findByLocation(GeoLocation geoLocation, float distance) {
        return null;
    }

}
