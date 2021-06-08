package com.gamakdragons.wheretruck.domain.truck.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.domain.truck.service.TruckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/truck")
@Slf4j
public class TruckController {

    @Autowired
    private TruckService truckService;

    @GetMapping("/{id}")
    public ResponseEntity<Truck> getById(@PathVariable String id) {
        log.info("/truck/" + id);

        Truck truck = truckService.getById(id);

        return new ResponseEntity<>(truck, HttpStatus.OK);
    }

    @GetMapping("/all" )
    public ResponseEntity<SearchResultDto<Truck>> getAllTrucks() {
        log.info("/truck/all");

        return new ResponseEntity<>(truckService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/geo")
    public ResponseEntity<SearchResultDto<Truck>> getByGeoLocation(GeoLocation location, float distance) {
        log.info("/truck/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(truckService.findByGeoLocation(location, distance), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<SearchResultDto<Truck>> getByUserId(String userId) {
        log.info("/truck/user. userId=" + userId);

        return new ResponseEntity<>(truckService.findByUserId(userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<IndexResultDto> save(@RequestBody Truck truck) {
        log.info("truck=" + truck);

        return new ResponseEntity<>(truckService.saveTruck(truck), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UpdateResultDto> update(@RequestBody Truck truck) {
        log.info("/truck. truck=" + truck);

        return new ResponseEntity<>(truckService.updateTruck(truck), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> delete(@PathVariable String id) {
        log.info("/truck/" + id);

        return new ResponseEntity<>(truckService.deleteTruck(id), HttpStatus.OK);
    }

    @PutMapping("/start/{truckId}")
    public ResponseEntity<UpdateResultDto> startTruck(@PathVariable String truckId, @RequestBody GeoLocation geoLocation) {
        log.info("/truck/start. id=" + truckId + ", geoLocation=" + geoLocation);

        return new ResponseEntity<>(truckService.openTruck(truckId, geoLocation), HttpStatus.OK);
    }

    @PutMapping("/stop/{truckId}")
    public ResponseEntity<UpdateResultDto> stopTruck(@PathVariable String truckId) {
        log.info("/truck/stop. id=" + truckId);

        return new ResponseEntity<>(truckService.stopTruck(truckId), HttpStatus.OK);
    }
}
