package com.gamakdragons.wheretruck.truck.model;

import com.gamakdragons.wheretruck.common.GeoLocation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class Truck {

    private String id;
    private String name;
    private float score;
    private int numRating;
    private GeoLocation geoLocation;
    private String description;
    private boolean opened;
    private String userId;
}
