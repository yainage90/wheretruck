package com.gamakdragons.wheretruck.food.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.food.model.Food;
import com.gamakdragons.wheretruck.food.service.FoodService;

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
@RequestMapping("/food")
@Slf4j
public class FoodController {

    @Autowired
    private FoodService service;

    @GetMapping("/get/id")
    public ResponseEntity<Food> getById(String id) {
        log.info("/food/search/id. id=" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/search/truckId")
    public ResponseEntity<SearchResultDto<Food>> getByTruckId(String truckId) {
        log.info("/food/search/truckId. truckId=" + truckId);

        return new ResponseEntity<>(service.findByTruckId(truckId), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody Food food) {
        log.info("/food/save. food=" + food);

        return new ResponseEntity<>(service.saveFood(food), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody Food food) {
        log.info("/food/update. food=" + food);

        return new ResponseEntity<>(service.updateFood(food), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResultDto> delete(String id) {
        log.info("/food/delete. id=" + id);

        return new ResponseEntity<>(service.deleteFood(id), HttpStatus.OK);
    }
    
}
