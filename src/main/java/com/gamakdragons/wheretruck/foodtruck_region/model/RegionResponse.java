package com.gamakdragons.wheretruck.foodtruck_region.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionResponse {
    
    private final long numFound;
    private final List<FoodTruckRegion> regions;
}
