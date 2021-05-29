package com.gamakdragons.wheretruck.foodtruck_region.controller;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.service.FoodTruckRegionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/region")
@Slf4j
public class FoodTruckRegionRestController {
    
    @Autowired
    private FoodTruckRegionService service;

    @RequestMapping(value = "/all", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> allRegions() {
        log.info("/region/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/geo")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByDistance(GeoLocation location, float distance) {
        log.info("/region/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(service.findByLocation(location, distance), HttpStatus.OK);
    }

    @PostMapping(value = "/geo")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByDistance(@RequestBody GeoRequest request) {
        log.info("/region/geo. request=" + request.toString());

        return new ResponseEntity<>(service.findByLocation(request.getGeoLocation(), request.getDistance()), HttpStatus.OK);

    }

    @GetMapping(value = "/address")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByAddress(@Nullable String city, @Nullable String town) {
        log.info("/region/address. city=" + city + ", town=" + town);

        return new ResponseEntity<>(service.findByAddress(city, town), HttpStatus.OK);
    }

    @PostMapping(value = "/address")
    public ResponseEntity<SearchResultDto<FoodTruckRegion>> getRegionsByAddress(@RequestBody AddressRequest request) {
        log.info("/region/addresss. request=" + request);

        return new ResponseEntity<>(service.findByAddress(request.getCity(), request.getTown()), HttpStatus.OK);
    }

}
