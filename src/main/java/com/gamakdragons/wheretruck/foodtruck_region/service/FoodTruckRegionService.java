package com.gamakdragons.wheretruck.foodtruck_region.service;

import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

public interface FoodTruckRegionService {
    
    List<FoodTruckRegion> findAll();
    List<FoodTruckRegion> findByLocation(GeoLocation location, float distance);
    List<FoodTruckRegion> findByAddress(String city, String town);
}
