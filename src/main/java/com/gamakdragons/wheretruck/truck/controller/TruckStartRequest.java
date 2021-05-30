package com.gamakdragons.wheretruck.truck.controller;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TruckStartRequest {
    
    private String id;
    private GeoLocation geoLocation;
}
