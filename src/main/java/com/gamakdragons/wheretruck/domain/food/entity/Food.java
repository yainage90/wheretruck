package com.gamakdragons.wheretruck.domain.food.entity;

import java.util.HashMap;
import java.util.Map;

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
    private String imageUrl;

    public Map<String, Object> toMap() {
        Map<String, Object> foodMap = new HashMap<>();
        
        foodMap.put("id", getId());
        foodMap.put("name", getName());
        foodMap.put("cost", getCost());
        foodMap.put("description", getDescription());
        foodMap.put("imageUrl", getImageUrl());

        return foodMap;
    }
}
