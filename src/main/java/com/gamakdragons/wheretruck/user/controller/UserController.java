package com.gamakdragons.wheretruck.user.controller;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.model.User;
import com.gamakdragons.wheretruck.user.service.UserService;

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
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping("/get/id")
    public ResponseEntity<User> getById(String id) {
        log.info("/user/get/id. id=" + id);

        return new ResponseEntity<>(service.getById(id), HttpStatus.OK);
    }

    @GetMapping("/search/all")
    public ResponseEntity<SearchResultDto<User>> getAll() {
        log.info("/user/search/all");

        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/email")
    public ResponseEntity<SearchResultDto<User>> getByEmail(String email) {
        log.info("/user/search/email. email=" + email);

        return new ResponseEntity<>(service.findByEmail(email), HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<IndexResultDto> save(@RequestBody User user) {
        log.info("/user/save. user=" + user);

        return new ResponseEntity<>(service.saveUser(user), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateResultDto> update(@RequestBody User user) {
        log.info("/user/update. user=" + user);

        return new ResponseEntity<>(service.updateUser(user), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteResultDto> delete(String id) {
        log.info("/user/delete. id=" + id);

        return new ResponseEntity<>(service.deleteUser(id), HttpStatus.OK);
    }
}
