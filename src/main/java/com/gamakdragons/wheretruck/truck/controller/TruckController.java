package com.gamakdragons.wheretruck.truck.controller;

import java.util.List;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.food.entity.Food;
import com.gamakdragons.wheretruck.food.service.FoodService;
import com.gamakdragons.wheretruck.rating.entity.Rating;
import com.gamakdragons.wheretruck.rating.service.RatingService;
import com.gamakdragons.wheretruck.truck.dto.TruckGetResponseDto;
import com.gamakdragons.wheretruck.truck.entity.Truck;
import com.gamakdragons.wheretruck.truck.service.TruckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    private FoodService foodService;
    
    @Autowired
    private RatingService ratingService;
    
    @GetMapping("/search/all" )
    public ResponseEntity<SearchResultDto<Truck>> getAllTrucks() {
        log.info("/truck/search/all");

        return new ResponseEntity<>(truckService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/geo")
    public ResponseEntity<SearchResultDto<Truck>> getByGeoLocation(GeoLocation location, float distance) {
        log.info("/truck/search/geo. geoLocation=" + location + ", distance=" + distance);

        return new ResponseEntity<>(truckService.findByLocation(location, distance), HttpStatus.OK);
    }

    @GetMapping("/search/userId")
    public ResponseEntity<SearchResultDto<Truck>> getByUserId(String userId) {
        log.info("/truck/search/userId. userId=" + userId);

        return new ResponseEntity<>(truckService.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/get/id")
    public ResponseEntity<TruckGetResponseDto> getById(String id) {
        log.info("/truck/search/id. id=" + id);

        Truck truck = truckService.getById(id);
        List<Food> foods = foodService.findByTruckId(id).getDocs();
        List<Rating> ratings = ratingService.findByTruckId(id).getDocs();

        return new ResponseEntity<>(truck.toGetResponseDto(foods, ratings), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody Truck truck) {
        log.info("/truck/save. dto=" + truck);

        return new ResponseEntity<>(truckService.saveTruck(truck), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody Truck truck) {
        log.info("/truck/update. truck=" + truck);

        return new ResponseEntity<>(truckService.updateTruck(truck), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResultDto> delete(String id) {
        log.info("/truck/delete. id=" + id);

        return new ResponseEntity<>(truckService.deleteTruck(id), HttpStatus.OK);
    }

    @PutMapping("/start")
    public ResponseEntity<UpdateResultDto> startTruck(@RequestBody TruckStartRequest request) {
        log.info("/truck/start. id=" + request.getId() + ", geoLocation=" + request.getGeoLocation());

        return new ResponseEntity<>(truckService.openTruck(request.getId(), request.getGeoLocation()), HttpStatus.OK);
    }

    @PutMapping("/stop")
    public ResponseEntity<UpdateResultDto> stopTruck(@RequestBody TruckStartRequest request) {
        log.info("/truck/stop. id=" + request.getId());

        return new ResponseEntity<>(truckService.stopTruck(request.getId()), HttpStatus.OK);
    }
}
