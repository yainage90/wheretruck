package com.gamakdragons.wheretruck.domain.user.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.domain.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable String id) {
        log.info("/user/" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @PutMapping("/nickname")
    public ResponseEntity<UpdateResultDto> update(String nickName, HttpServletRequest request) {

        String userId = request.getAttribute("userId").toString();

        log.info("/api/user. nickName=" + nickName);

        return new ResponseEntity<>(service.changeNickName(userId, nickName), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> delete(@PathVariable String id) {
        log.info("/user/" + id);

        return new ResponseEntity<>(service.deleteUser(id), HttpStatus.OK);
    }

    @PutMapping("/favorite/{truckId}")
    public ResponseEntity<UpdateResultDto> addFavorite(@PathVariable String userId, @PathVariable String truckId) {
        log.info("/" + userId + "/favorite/" + truckId);

        return new ResponseEntity<>(service.addFavorite(userId, truckId), HttpStatus.OK);
    }

    @DeleteMapping("/favorite/{truckId}")
    public ResponseEntity<UpdateResultDto> deleteFavorite(@PathVariable String truckId, HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();
        log.info("/favorite/" + truckId);
        return new ResponseEntity<>(service.deleteFavorite(userId, truckId), HttpStatus.OK);
    }
}
