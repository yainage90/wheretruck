package com.gamakdragons.wheretruck.truck.model;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Truck {

    private String id;
    private String name;
    private GeoLocation geoLocation;
    private String description;
    private boolean isOpened;

    public TruckIndexRequestDto toIndexRequestDto() {

        return TruckIndexRequestDto.builder()
                .name(name)
                .geoLocation(geoLocation)
                .description(description)
                .isOpened(isOpened)
                .build();
    }
}
