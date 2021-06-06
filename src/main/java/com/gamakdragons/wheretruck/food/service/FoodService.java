package com.gamakdragons.wheretruck.food.service;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.food.dto.FoodUpdateRequestDto;

public interface FoodService {
    
    UpdateResultDto saveFood(String truckId, FoodSaveRequestDto foodSaveRequestDto);
    UpdateResultDto updateFood(String truckId, FoodUpdateRequestDto foodUpdateRequestDto);
    UpdateResultDto deleteFood(String truckId, String id);
}
