package com.gamakdragons.wheretruck.domain.region.service;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.region.entity.Region;

public interface RegionService {
    
    SearchResultDto<Region> findAll();
    SearchResultDto<Region> findByLocation(GeoLocation location, float distance);
    SearchResultDto<Region> findByAddress(String city, String town);
}
