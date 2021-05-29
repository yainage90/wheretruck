package com.gamakdragons.wheretruck.foodtruck_region.service;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.model.RegionResponse;

public interface FoodTruckRegionService {
    
    RegionResponse findAll();
    RegionResponse findByLocation(GeoLocation location, float distance);
    RegionResponse findByAddress(String city, String town);
}
