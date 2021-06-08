package com.gamakdragons.wheretruck.domain.food.service;

import java.util.List;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodUpdateRequestDto;

public interface FoodService {
    
    UpdateResultDto saveFood(String truckId, FoodSaveRequestDto foodSaveRequestDto);
    UpdateResultDto updateFood(String truckId, FoodUpdateRequestDto foodUpdateRequestDto);
    UpdateResultDto deleteFood(String truckId, String id);
    UpdateResultDto sortFoods(String truckId, List<String> ids);
}
