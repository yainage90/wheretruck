package com.gamakdragons.wheretruck.domain.rating.controller;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.domain.rating.service.RatingService;

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
@RequestMapping("/rating")
@Slf4j
public class RatingController {
    
    @Autowired
    private RatingService service;


    @PostMapping("/{truckId}")
    public ResponseEntity<UpdateResultDto> save(@PathVariable String truckId, @RequestBody Rating rating) {
        log.info("/rating/" + truckId + ". rating=" + rating);

        return new ResponseEntity<>(service.saveRating(truckId, rating), HttpStatus.OK);
    }

    @PutMapping("/{truckId}")
    public ResponseEntity<UpdateResultDto> update(@PathVariable String truckId, @RequestBody Rating rating) {
        log.info("/rating/" + truckId + ". rating=" + rating);

        return new ResponseEntity<>(service.updateRating(truckId, rating), HttpStatus.OK);
    }

    @DeleteMapping("/{truckId}/{id}")
    public ResponseEntity<UpdateResultDto> delete(@PathVariable String truckId, @PathVariable String id) {
        log.info("/rating/" + truckId + "/" + id);

        return new ResponseEntity<>(service.deleteRating(truckId, id), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SearchResultDto<MyRatingDto>> findByUserId(@PathVariable String userId) {
        log.info("/rating/user/" + userId);

        return new ResponseEntity<>(service.findByUserId(userId), HttpStatus.OK);
    }

}
