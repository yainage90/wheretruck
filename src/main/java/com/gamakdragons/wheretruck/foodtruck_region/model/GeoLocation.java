package com.gamakdragons.wheretruck.foodtruck_region.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class GeoLocation {
    
    private float lon;
    private float lat;
}
