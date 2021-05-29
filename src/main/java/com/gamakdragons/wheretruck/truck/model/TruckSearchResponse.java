package com.gamakdragons.wheretruck.truck.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TruckSearchResponse {
    
    private long numFound;
    private List<Truck> trucks;
}
