package com.gamakdragons.wheretruck.foodtruck_region.util;

import java.util.ArrayList;
import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.google.gson.Gson;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticSearchUtil {
    
    public static List<FoodTruckRegion> getFoodTruckRegionFromResponse(SearchResponse response) {
        List<FoodTruckRegion> regions = new ArrayList<>();
        for(SearchHit hit : response.getHits().getHits()) {
            try {
                FoodTruckRegion pojo = new Gson().fromJson(hit.getSourceAsString(), FoodTruckRegion.class);
                regions.add(pojo);
            } catch(Exception e) {
                log.error(e.getMessage());
            }
        }

        return regions;
    }
}
