package com.gamakdragons.wheretruck.foodtruck_region.controller;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.service.FoodTruckRegionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/region")
@Slf4j
public class RegionController {
    
    @Autowired
    private FoodTruckRegionService service;

    @GetMapping("/search/all")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> allRegions() {
        log.info("/region/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/geo")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByDistance(GeoLocation location, float distance) {
        log.info("/region/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(service.findByLocation(location, distance), HttpStatus.OK);
    }

    @GetMapping("/search/address")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByAddress(@Nullable String city, @Nullable String town) {
        log.info("/region/address. city=" + city + ", town=" + town);

        return new ResponseEntity<>(service.findByAddress(city, town), HttpStatus.OK);
    }

}
