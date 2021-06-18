package com.gamakdragons.wheretruck.domain.truck.service;

import java.util.List;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;

public interface TruckService {
    
    SearchResultDto<Truck> findAll();
    SearchResultDto<Truck> findByUserId(String userId);
    SearchResultDto<Truck> findByGeoLocation(GeoLocation geoLocation, float distance);

    Truck getById(String id);
    SearchResultDto<Truck> getByIds(List<String> ids);

    IndexUpdateResultDto saveTruck(TruckSaveRequestDto truckSaveRequestDto);
    IndexUpdateResultDto updateTruck(TruckSaveRequestDto truckSaveRequestDto);
    DeleteResultDto deleteTruck(String id);

    IndexUpdateResultDto openTruck(String id, GeoLocation geoLocation);
    IndexUpdateResultDto stopTruck(String id);
}
