package com.gamakdragons.wheretruck.foodtruck_region.util;

import java.util.ArrayList;
import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.RegionResponse;
import com.google.gson.Gson;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchUtil {
    
    public static RegionResponse createResultFromResponse(SearchResponse searchResponse) {
        long numFound = searchResponse.getHits().getTotalHits().value;

        List<FoodTruckRegion> regions = new ArrayList<>();
        for(SearchHit hit : searchResponse.getHits().getHits()) {
            FoodTruckRegion pojo = new Gson().fromJson(hit.getSourceAsString(), FoodTruckRegion.class);
            regions.add(pojo);
        }

        return RegionResponse.builder()
                .numFound(numFound)
                .regions(regions)
                .build();

    }
}
