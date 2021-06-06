package com.gamakdragons.wheretruck.domain.truck.controller;

import com.gamakdragons.wheretruck.common.GeoLocation;

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
