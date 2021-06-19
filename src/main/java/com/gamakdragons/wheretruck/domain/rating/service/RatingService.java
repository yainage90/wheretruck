package com.gamakdragons.wheretruck.domain.rating.service;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;

public interface RatingService {
    
    IndexUpdateResultDto saveRating(String truckId, Rating rating);
    IndexUpdateResultDto updateRating(String truckId, Rating rating);
    IndexUpdateResultDto deleteRating(String truckId, String id);

    SearchResultDto<MyRatingDto> findByUserId(String userId);
}
