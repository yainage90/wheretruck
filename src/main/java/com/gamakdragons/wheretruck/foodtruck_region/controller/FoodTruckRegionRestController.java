package com.gamakdragons.wheretruck.foodtruck_region.controller;

import java.util.List;

import com.gamakdragons.wheretruck.foodtruck_region.model.FoodTruckRegion;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.foodtruck_region.service.FoodTruckRegionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/region")
public class FoodTruckRegionRestController {
    
    @Autowired
    private FoodTruckRegionService service;

    @GetMapping(value = "/all")
    public List<FoodTruckRegion> getAllRegions() {
        List<FoodTruckRegion> regions = service.findAll();
        return regions;
    }

    @GetMapping(value = "/geo")
    public List<FoodTruckRegion> getRegionsByDistance(GeoLocation geoLocation, float distance) {
        List<FoodTruckRegion> regions = service.findByLocation(geoLocation, distance);
        return regions;
    }
}
