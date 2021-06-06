package com.gamakdragons.wheretruck.food.entity;

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

    public Map<String, String> toMap() {
        Map<String, String> foodMap = new HashMap<>();
        
        foodMap.put("id", getId());
        foodMap.put("name", getName());
        foodMap.put("cost", String.valueOf(cost));
        foodMap.put("description", description);
        foodMap.put("imageUrl", imageUrl);

        return foodMap;
    }
}
