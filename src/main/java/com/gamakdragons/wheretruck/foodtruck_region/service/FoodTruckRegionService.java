package com.gamakdragons.wheretruck.foodtruck_region.service;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

public interface FoodTruckRegionService {
    
    SearchResultDto<FoodTruckRegion> findAll();
    SearchResultDto<FoodTruckRegion> findByLocation(GeoLocation location, float distance);
    SearchResultDto<FoodTruckRegion> findByAddress(String city, String town);
}
