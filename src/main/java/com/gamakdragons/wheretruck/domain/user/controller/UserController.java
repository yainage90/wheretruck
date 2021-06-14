package com.gamakdragons.wheretruck.domain.user.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.domain.user.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getById(HttpServletRequest httpServletRequest) {
        log.info("/user/me");

        String userId = httpServletRequest.getAttribute("userId").toString();

        return new ResponseEntity<>(userService.getById(userId), HttpStatus.OK);
    }

    @PutMapping("/nickname")
    public ResponseEntity<UpdateResultDto> updateNickName(@RequestBody User user, HttpServletRequest request) {

        String userId = request.getAttribute("userId").toString();

        log.info("/api/user/nickName. nickName=" + user.getNickName());

        return new ResponseEntity<>(userService.changeNickName(userId, user.getNickName()), HttpStatus.OK);
    }

    @PutMapping("/role")
    public ResponseEntity<UpdateResultDto> updateRole(@RequestBody User user, HttpServletRequest request) {

        String userId = request.getAttribute("userId").toString();

        log.info("/api/user/role. role =" + user.getRole());

        return new ResponseEntity<>(userService.changeRole(userId, user.getRole()), HttpStatus.OK);
    }
}
