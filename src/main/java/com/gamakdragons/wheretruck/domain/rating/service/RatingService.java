package com.gamakdragons.wheretruck.domain.rating.service;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;

public interface RatingService {
    
    UpdateResultDto saveRating(String truckId, Rating rating);
    //UpdateResultDto updateRating(String truckId, Rating rating);
    UpdateResultDto deleteRating(String truckId, String id);

    SearchResultDto<MyRatingDto> findByUserId(String userId);
}
