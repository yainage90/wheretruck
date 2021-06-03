package com.gamakdragons.wheretruck.food.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.food.entity.Food;

public interface FoodService {
    
    Food getById(String id);
    SearchResultDto<Food> findByTruckId(String truckId);

    IndexResultDto saveFood(Food food);
    UpdateResultDto updateFood(Food food);
    DeleteResultDto deleteFood(String id);
}
