package com.gamakdragons.wheretruck.domain.region.controller;

import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.region.entity.Region;
import com.gamakdragons.wheretruck.domain.region.service.RegionService;

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
    private RegionService service;

    @GetMapping("/all")
    public ResponseEntity<SearchResultDto<Region>> allRegions() {
        log.info("/region/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/geo")
    public ResponseEntity<SearchResultDto<Region>> getRegionsByDistance(GeoLocation location, float distance) {
        log.info("/region/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(service.findByLocation(location, distance), HttpStatus.OK);
    }

    @GetMapping("/address")
    public ResponseEntity<SearchResultDto<Region>> getRegionsByAddress(@Nullable String city, @Nullable String town) {
        log.info("/region/address. city=" + city + ", town=" + town);

        return new ResponseEntity<>(service.findByAddress(city, town), HttpStatus.OK);
    }

}
