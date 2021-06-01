package com.gamakdragons.wheretruck.rating.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.model.Rating;

public interface RatingService {
    
    Rating getById(String id);
    SearchResultDto<Rating> findByTruckId(String truckId);
    SearchResultDto<Rating> findByUserId(String userId);

    IndexResultDto saveRating(Rating rating);
    UpdateResultDto updateRating(Rating rating);
    DeleteResultDto deleteRating(String id);

}
