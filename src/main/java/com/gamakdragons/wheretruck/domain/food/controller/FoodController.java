package com.gamakdragons.wheretruck.domain.food.controller;

import java.util.List;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.service.FoodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/food")
@Slf4j
public class FoodController {

    @Autowired
    private FoodService service;

    @RequestMapping(value = {"/{truckId}"}, method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<UpdateResultDto> save(@PathVariable String truckId, FoodSaveRequestDto foodSaveRequestDto) {
        log.info("/api/food/" + truckId);

        return new ResponseEntity<>(service.saveFood(truckId, foodSaveRequestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{truckId}/{id}")
    public ResponseEntity<UpdateResultDto> delete(@PathVariable String truckId, @PathVariable String id) {
        log.info("/api/food/" + truckId + "/" + id);

        return new ResponseEntity<>(service.deleteFood(truckId, id), HttpStatus.OK);
    }

    @PutMapping("/{truckId}/sort/{ids}")
    public ResponseEntity<UpdateResultDto> sort(@PathVariable String truckId, @PathVariable List<String> ids) {
        log.info("/api/food/" + truckId + "/" + ids);

        return new ResponseEntity<>(service.sortFoods(truckId, ids), HttpStatus.OK);
    }
    
}
