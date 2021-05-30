package com.gamakdragons.wheretruck.truck.controller;

import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.foodtruck_region.controller.GeoRequest;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.truck.model.TruckIndexRequestDto;
import com.gamakdragons.wheretruck.truck.service.TruckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/truck")
@Slf4j
public class TruckController {

    @Autowired
    private TruckService service;
    
    @RequestMapping(value = "/all", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<SearchResultDto<Truck>> getAllTrucks() {
        log.info("/truck/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/geo")
    public ResponseEntity<SearchResultDto<Truck>> getByGeoLocation(GeoLocation location, float distance) {
        log.info("/truck/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(service.findByLocation(location, distance), HttpStatus.OK);
    }

    @PostMapping("/geo")
    public ResponseEntity<SearchResultDto<Truck>> getByGeoLocation(@RequestBody GeoRequest request) {
        log.info("/truck/geo. request=" + request);

        return new ResponseEntity<>(service.findByLocation(request.getGeoLocation(), request.getDistance()), HttpStatus.OK);
    }

    @RequestMapping(value = "/id", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Truck> getById(String id) {
        log.info("/truck/id. id=" + id);

        return new ResponseEntity<>(service.findById(id), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody TruckIndexRequestDto dto) {
        log.info("/truck/save. dto=" + dto);

        return new ResponseEntity<>(service.registerTruck(dto), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody Truck truck) {
        log.info("/truck/update. truck=" + truck);

        return new ResponseEntity<>(service.updateTruck(truck), HttpStatus.OK);
    }

}
