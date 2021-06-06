package com.gamakdragons.wheretruck.truck.entity;

import java.util.List;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.food.entity.Food;
import com.gamakdragons.wheretruck.rating.entity.Rating;

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
    private GeoLocation geoLocation;
    private String description;
    private boolean opened;
    private String userId;
    private int numRating;
    private float starAvg;
    private List<Food> foods;
    private List<Rating> ratings;

}
