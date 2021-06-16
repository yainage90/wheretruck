package com.gamakdragons.wheretruck.domain.rating.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.domain.rating.service.RatingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/rating")
@Slf4j
public class RatingController {
    
    @Autowired
    private RatingService service;


    @RequestMapping(value = "/{truckId}", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<IndexUpdateResultDto> save(@PathVariable String truckId, @RequestBody Rating rating, HttpServletRequest httpServletRequest) {
        log.info("/api/rating/" + truckId + ". rating=" + rating);

        rating.setUserId(httpServletRequest.getAttribute("userId").toString());

        return new ResponseEntity<>(service.saveRating(truckId, rating), HttpStatus.OK);
    }

    @DeleteMapping("/{truckId}/{id}")
    public ResponseEntity<IndexUpdateResultDto> delete(@PathVariable String truckId, @PathVariable String id) {
        log.info("/api/rating/" + truckId + "/" + id);

        return new ResponseEntity<>(service.deleteRating(truckId, id), HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<SearchResultDto<MyRatingDto>> findByUserId(HttpServletRequest request) {

        String userId = request.getAttribute("userId").toString();

        log.info("/api/rating/user/" + userId);

        return new ResponseEntity<>(service.findByUserId(userId), HttpStatus.OK);
    }

}
