package com.gamakdragons.wheretruck.favorite.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.favorite.model.Favorite;
import com.gamakdragons.wheretruck.favorite.service.FavoriteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/favorite")
@Slf4j
public class FavoriteController {
    
    @Autowired
    private FavoriteService service;

    @GetMapping("/get/id")
    public ResponseEntity<Favorite> getById(String id) {
        log.info("/favorite/get/id. id=" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/search/all")
    public ResponseEntity<SearchResultDto<Favorite>> getAll() {
        log.info("/favorite/search/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/userId")
    public ResponseEntity<SearchResultDto<Favorite>> getByUserId(String userId) {
        log.info("/favorite/search/userId. userId=" + userId);

        return new ResponseEntity<>(service.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/search/truckId")
    public ResponseEntity<SearchResultDto<Favorite>> getByTruckId(String truckId) {
        log.info("/favorite/search/truckId. truckId=" + truckId);

        return new ResponseEntity<>(service.findByTruckId(truckId), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody Favorite favorite) {
        log.info("/favorite/save. favorite=" + favorite);

        return new ResponseEntity<>(service.saveFavorite(favorite), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResultDto> delete(String id) {
        log.info("/favorite/delete. id=" + id);

        return new ResponseEntity<>(service.deleteFavorite(id), HttpStatus.OK);
    }

}
