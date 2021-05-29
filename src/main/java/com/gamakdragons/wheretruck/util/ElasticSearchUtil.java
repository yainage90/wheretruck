package com.gamakdragons.wheretruck.util;

import java.util.ArrayList;
import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.RegionResponse;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.truck.model.TruckSearchResponse;
import com.google.gson.Gson;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

public class ElasticSearchUtil {
    
    public static RegionResponse createRegionResponseFromSearchResponse(SearchResponse searchResponse) {
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

    public static TruckSearchResponse createTruckResponseFromSearchResponse(SearchResponse searchResponse) {
        long numFound = searchResponse.getHits().getTotalHits().value;
        List<Truck> trucks = new ArrayList<>();
        for(SearchHit hit : searchResponse.getHits().getHits()) {
            Truck pojo = new Gson().fromJson(hit.getSourceAsString(), Truck.class);
            trucks.add(pojo);
        }

        return TruckSearchResponse.builder()
            .numFound(numFound)
            .trucks(trucks)
            .build();
    }



}
