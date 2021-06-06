package com.gamakdragons.wheretruck.truck.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.truck.entity.Truck;

public interface TruckService {
    
    SearchResultDto<Truck> findAll();
    SearchResultDto<Truck> findByUserId(String userId);
    SearchResultDto<Truck> findByLocation(GeoLocation location, float distance);

    Truck getById(String id);

    IndexResultDto saveTruck(Truck truck);
    UpdateResultDto updateTruck(Truck truck);
    DeleteResultDto deleteTruck(String id);

    UpdateResultDto openTruck(String id, GeoLocation location);
    UpdateResultDto stopTruck(String id);
}
