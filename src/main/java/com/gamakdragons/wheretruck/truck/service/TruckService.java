package com.gamakdragons.wheretruck.truck.service;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.truck.model.TruckIndexResponse;
import com.gamakdragons.wheretruck.truck.model.TruckSearchResponse;

public interface TruckService {
    
    TruckSearchResponse findAll();
    Truck findById(String id);
    TruckSearchResponse findByLocation(GeoLocation location, float distance);
    Truck openTruck(String id, GeoLocation location);
    Truck stopTruck(String id);
    TruckIndexResponse registerTruck(Truck truck);
    TruckSearchResponse updateTruck(Truck truck);
    TruckSearchResponse deleteTruck(String id);
}
