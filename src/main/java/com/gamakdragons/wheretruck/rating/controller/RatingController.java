package com.gamakdragons.wheretruck.rating.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.model.Rating;
import com.gamakdragons.wheretruck.rating.service.RatingService;

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
@RequestMapping("/rating")
@Slf4j
public class RatingController {
    
    @Autowired
    private RatingService service;

    @GetMapping("/get/id")
    public ResponseEntity<Rating> getById(String id) {
        log.info("/rating/search/id. id=" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/search/truckId")
    public ResponseEntity<SearchResultDto<Rating>> getByTruckId(String truckId) {
        log.info("/rating/search/truckId. truckId=" + truckId);

        return new ResponseEntity<>(service.findByTruckId(truckId), HttpStatus.OK);
    }

    @GetMapping("/search/userId")
    public ResponseEntity<SearchResultDto<Rating>> getByUserId(String userId) {
        log.info("/rating/search/truckId. userId=" + userId);

        return new ResponseEntity<>(service.findByUserId(userId), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody Rating rating) {
        log.info("/rating/save. rating=" + rating);

        return new ResponseEntity<>(service.saveRating(rating), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody Rating rating) {
        log.info("/rating/update. rating=" + rating);

        return new ResponseEntity<>(service.updateRating(rating), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResultDto> delete(String id) {
        log.info("/rating/delete. id=" + id);

        return new ResponseEntity<>(service.deleteRating(id), HttpStatus.OK);
    }

}
