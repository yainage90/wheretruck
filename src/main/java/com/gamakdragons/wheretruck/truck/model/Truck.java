package com.gamakdragons.wheretruck.truck.model;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class Truck {

    private String id;
    private String name;
    private GeoLocation geoLocation;
    private String description;
    private boolean opened;
    private String userId;
}
