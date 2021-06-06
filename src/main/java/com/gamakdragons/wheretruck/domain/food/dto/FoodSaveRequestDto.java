package com.gamakdragons.wheretruck.domain.food.dto;

import java.util.UUID;

import com.gamakdragons.wheretruck.domain.food.entity.Food;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FoodSaveRequestDto {
    
    private String id;
    private String name;
    private int cost;
    private String description;
    private MultipartFile image;

    public Food toEntity() {
        return Food.builder()
                    .id(UUID.randomUUID().toString())
                    .name(getName())
                    .cost(getCost())
                    .description(getDescription())
                    .build();
    }
}
