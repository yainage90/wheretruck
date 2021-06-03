package com.gamakdragons.wheretruck.truck.dto;

import java.util.List;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.food.entity.Food;
import com.gamakdragons.wheretruck.rating.entity.Rating;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TruckGetResponseDto {
    
    private String id;
    private String name;
    private GeoLocation geoLocation;
    private String description;
    private boolean opened;
    private String userId;
    private int numRating;
    private float score;

    private List<Food> foods;
    private List<Rating> ratings;

}
