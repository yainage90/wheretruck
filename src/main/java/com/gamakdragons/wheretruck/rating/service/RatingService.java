package com.gamakdragons.wheretruck.rating.service;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.entity.Rating;

public interface RatingService {
    
    UpdateResultDto saveRating(Rating rating);
    UpdateResultDto updateRating(Rating rating);
    UpdateResultDto deleteRating(String truckId, String id);
}
