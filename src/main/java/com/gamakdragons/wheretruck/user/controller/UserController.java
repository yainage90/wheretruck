package com.gamakdragons.wheretruck.user.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.entity.User;
import com.gamakdragons.wheretruck.user.service.UserService;

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
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        log.info("/user/" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<IndexResultDto> save(@RequestBody User user) {
        log.info("/user. user=" + user);

        return new ResponseEntity<>(service.saveUser(user), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UpdateResultDto> update(@RequestBody User user) {
        log.info("/user. user=" + user);

        return new ResponseEntity<>(service.updateUser(user), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> delete(@PathVariable String id) {
        log.info("/user/" + id);

        return new ResponseEntity<>(service.deleteUser(id), HttpStatus.OK);
    }

    @PutMapping("/{userId}/favorite/{truckId}")
    public ResponseEntity<UpdateResultDto> addFavorite(@PathVariable String userId, @PathVariable String truckId) {
        log.info("/" + userId + "/favorite/" + truckId);

        return new ResponseEntity<>(service.addFavorite(userId, truckId), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/favorite/{truckId}")
    public ResponseEntity<UpdateResultDto> deleteFavorite(@PathVariable String userId, @PathVariable String truckId) {
        log.info("/" + userId + "/favorite/" + truckId);
        return new ResponseEntity<>(service.deleteFavorite(userId, truckId), HttpStatus.OK);
    }
}
