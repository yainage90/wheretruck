package com.gamakdragons.wheretruck.domain.truck.service;

import java.util.List;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;

public interface TruckService {
    
    SearchResultDto<Truck> findAll();
    SearchResultDto<Truck> findByUserId(String userId);
    SearchResultDto<Truck> findByGeoLocation(GeoLocation location, float distance);

    Truck getById(String id);
    SearchResultDto<Truck> getByIds(List<String> ids);

    IndexResultDto saveTruck(Truck truck);
    UpdateResultDto updateTruck(Truck truck);
    DeleteResultDto deleteTruck(String id);

    UpdateResultDto openTruck(String id, GeoLocation location);
    UpdateResultDto stopTruck(String id);
}
