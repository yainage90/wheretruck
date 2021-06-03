package com.gamakdragons.wheretruck.food.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class Food {
    
    private String id;
    private String name;
    private int cost;
    private String description;
    private byte[] image;
    private String truckId;

}
