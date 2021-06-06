package com.gamakdragons.wheretruck.rating.controller;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.entity.Rating;
import com.gamakdragons.wheretruck.rating.service.RatingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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


    @PostMapping("/save")
    public ResponseEntity<UpdateResultDto> save(@RequestBody Rating rating) {
        log.info("/rating/save. rating=" + rating);

        return new ResponseEntity<>(service.saveRating(rating), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody Rating rating) {
        log.info("/rating/update. rating=" + rating);

        return new ResponseEntity<>(service.updateRating(rating), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<UpdateResultDto> delete(String truckId, String id) {
        log.info("/rating/delete. id=" + id);

        return new ResponseEntity<>(service.deleteRating(truckId, id), HttpStatus.OK);
    }

}
