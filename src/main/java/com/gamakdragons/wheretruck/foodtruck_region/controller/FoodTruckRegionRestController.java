package com.gamakdragons.wheretruck.foodtruck_region.controller;

import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
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
    public List<FoodTruckRegion> getAllRegions() {
        log.info("/region/all");
        List<FoodTruckRegion> regions = service.findAll();
        return regions;
    }

    @GetMapping(value = "/geo")
    public List<FoodTruckRegion> getRegionsByDistance(GeoLocation geoLocation, float distance) {
        log.info("/region/geo. geoLocation=" + geoLocation + ", distance=" + distance);
        List<FoodTruckRegion> regions = service.findByLocation(geoLocation, distance);
        return regions;
    }

    @PostMapping(value = "/geo")
    public List<FoodTruckRegion> getRegionsByDistance(@RequestBody GeoRequest request) {
        log.info("/region/geo. request=" + request.toString());
        List<FoodTruckRegion> regions = service.findByLocation(request.getGeoLocation(), request.getDistance());
        return regions;
    }

    @GetMapping(value = "/address")
    public List<FoodTruckRegion> getRegionsByAddress(@Nullable String city, @Nullable String town) {
        log.info("/region/address. city=" + city + ", town=" + town);
        List<FoodTruckRegion> regions = service.findByAddress(city, town);
        return regions;
    }

    @PostMapping(value = "/address")
    public List<FoodTruckRegion> getRegionsByAddress(@RequestBody AddressRequest request) {
        log.info("/region/addresss. request=" + request);
        List<FoodTruckRegion> regions = service.findByAddress(request.getCity(), request.getTown());
        return regions;
    }

}
