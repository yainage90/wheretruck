package com.gamakdragons.wheretruck.foodtruck_region.controller;

import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.model.RegionResponse;
import com.gamakdragons.wheretruck.foodtruck_region.service.FoodTruckRegionService;

import org.springframework.beans.factory.annotation.Autowired;
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
    public RegionResponse getAllRegions() {
        log.info("/region/all");
        RegionResponse regions = service.findAll();
        return regions;
    }

    @GetMapping(value = "/geo")
    public RegionResponse getRegionsByDistance(GeoLocation geoLocation, float distance) {
        log.info("/region/geo. geoLocation=" + geoLocation + ", distance=" + distance);
        RegionResponse regions = service.findByLocation(geoLocation, distance);
        return regions;
    }

    @PostMapping(value = "/geo")
    public RegionResponse getRegionsByDistance(@RequestBody GeoRequest request) {
        log.info("/region/geo. request=" + request.toString());
        RegionResponse regions = service.findByLocation(request.getGeoLocation(), request.getDistance());
        return regions;
    }

    @GetMapping(value = "/address")
    public RegionResponse getRegionsByAddress(@Nullable String city, @Nullable String town) {
        log.info("/region/address. city=" + city + ", town=" + town);
        RegionResponse regions = service.findByAddress(city, town);
        return regions;
    }

    @PostMapping(value = "/address")
    public RegionResponse getRegionsByAddress(@RequestBody AddressRequest request) {
        log.info("/region/addresss. request=" + request);
        RegionResponse regions = service.findByAddress(request.getCity(), request.getTown());
        return regions;
    }

}
