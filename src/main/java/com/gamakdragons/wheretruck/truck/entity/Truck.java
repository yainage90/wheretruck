package com.gamakdragons.wheretruck.truck.entity;

import java.util.List;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.food.entity.Food;
import com.gamakdragons.wheretruck.rating.entity.Rating;
import com.gamakdragons.wheretruck.truck.dto.TruckGetResponseDto;

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
    private float score;

    public TruckGetResponseDto toGetResponseDto(List<Food> foods, List<Rating> ratings) {

        return TruckGetResponseDto.builder()
                .id(getId())
                .name(getName())
                .geoLocation(getGeoLocation())
                .description(getDescription())
                .opened(isOpened())
                .userId(getUserId())
                .numRating(getNumRating())
                .score(getScore())
                .foods(foods)
                .ratings(ratings)
                .build();
    }

}
