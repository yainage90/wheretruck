package com.gamakdragons.wheretruck.domain.favorite.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;

public interface FavoriteService {
	
	SearchResultDto<Favorite> findByUserId(String userId);
	SearchResultDto<Favorite> findByTruckId(String truckId);

	IndexUpdateResultDto saveFavorite(Favorite favorite);
	DeleteResultDto deleteFavorite(String id);
}
