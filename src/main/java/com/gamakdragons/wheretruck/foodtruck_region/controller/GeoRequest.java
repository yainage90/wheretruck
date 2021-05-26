package com.gamakdragons.wheretruck.foodtruck_region.controller;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GeoRequest {

    private GeoLocation geoLocation;
    private int distance;
}
