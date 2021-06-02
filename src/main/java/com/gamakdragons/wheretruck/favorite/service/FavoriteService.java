package com.gamakdragons.wheretruck.favorite.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.favorite.model.Favorite;

public interface FavoriteService {
    
    SearchResultDto<Favorite> findAll();

    Favorite getById(String id);

    SearchResultDto<Favorite> findByUserId(String userId);
    SearchResultDto<Favorite> findByTruckId(String truckId);

    IndexResultDto saveFavorite(Favorite favorite);
    DeleteResultDto deleteFavorite(String id);
}
