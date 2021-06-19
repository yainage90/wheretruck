package com.gamakdragons.wheretruck.domain.food.service;

import java.util.List;

import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;

public interface FoodService {
    
    IndexUpdateResultDto saveFood(String truckId, FoodSaveRequestDto foodSaveRequestDto);
    IndexUpdateResultDto updateFood(String truckId, FoodSaveRequestDto foodSaveRequestDto);
    IndexUpdateResultDto deleteFood(String truckId, String id);
    IndexUpdateResultDto sortFoods(String truckId, List<String> ids);
}
