package com.gamakdragons.wheretruck.truck.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.truck.model.TruckIndexRequestDto;

public interface TruckService {
    
    SearchResultDto<Truck> findAll();
    SearchResultDto<Truck> findByLocation(GeoLocation location, float distance);

    Truck findById(String id);
    Truck openTruck(String id, GeoLocation location);
    Truck stopTruck(String id);

    IndexResultDto registerTruck(TruckIndexRequestDto truckIndexRequestDto);
    UpdateResultDto updateTruck(Truck truck);
    DeleteResultDto deleteTruck(String id);
}
