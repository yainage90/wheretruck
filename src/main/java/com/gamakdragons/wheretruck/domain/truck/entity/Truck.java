package com.gamakdragons.wheretruck.domain.truck.entity;

import java.util.List;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.domain.food.entity.Food;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
