package com.gamakdragons.wheretruck.domain.food.controller;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodUpdateRequestDto;
import com.gamakdragons.wheretruck.domain.food.service.FoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/save/{truckId}")
    public ResponseEntity<UpdateResultDto> save(@PathVariable String truckId, @RequestBody FoodSaveRequestDto foodSaveRequestDto) {
        log.info("/food/save.");

        return new ResponseEntity<>(service.saveFood(truckId, foodSaveRequestDto), HttpStatus.OK);
    }

    @PutMapping("/update/{truckId}")
    public ResponseEntity<UpdateResultDto> update(@PathVariable String truckId, @RequestBody FoodUpdateRequestDto foodUpdateRequestDto) {
        log.info("/food/update.");

        return new ResponseEntity<>(service.updateFood(truckId, foodUpdateRequestDto), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{truckId}/{id}")
    public ResponseEntity<UpdateResultDto> delete(@PathVariable String truckId, @PathVariable String id) {
        log.info("/food/delete. id=" + id);

        return new ResponseEntity<>(service.deleteFood(truckId, id), HttpStatus.OK);
    }
    
}
