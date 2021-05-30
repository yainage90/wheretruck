package com.gamakdragons.wheretruck.truck.model;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TruckIndexRequestDto {
    
    private String name;
    private GeoLocation geoLocation;
    private String description;
    private boolean isOpened;

}
