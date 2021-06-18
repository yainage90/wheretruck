package com.gamakdragons.wheretruck.domain.favorite.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;

public interface FavoriteService {
	
	SearchResultDto<Truck> findByUserId(String userId);
	int countByTruckId(String truckId);

	IndexUpdateResultDto saveFavorite(Favorite favorite);
	DeleteResultDto deleteFavorite(String truckId);
}
