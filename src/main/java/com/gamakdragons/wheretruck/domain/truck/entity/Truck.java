package com.gamakdragons.wheretruck.domain.truck.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
    private String imageUrl;

    @JsonInclude(Include.NON_NULL)
    private List<Food> foods;

    @JsonInclude(Include.NON_NULL)
    private List<Rating> ratings;

}
