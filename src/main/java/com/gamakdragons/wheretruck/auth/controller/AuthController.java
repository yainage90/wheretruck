package com.gamakdragons.wheretruck.auth.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.auth.dto.LoginRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LoginResponseDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutResponseDto;
import com.gamakdragons.wheretruck.auth.service.OAuth2Service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

	private final OAuth2Service service;

	@PutMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		log.info("/login. loginRequestDto=" + loginRequestDto);

		return new ResponseEntity<>(service.login(loginRequestDto), HttpStatus.OK);
	}

	@PutMapping("/logout")
	public ResponseEntity<LogoutResponseDto> logout(@RequestBody LogoutRequestDto logoutRequestDto, HttpServletRequest httpServletRequest) {
		String userId = httpServletRequest.getParameter("userId");

		log.info("/logout. logoutRequestDto=" + logoutRequestDto + ", userId=" + userId);

		return new ResponseEntity<>(service.logout(userId, logoutRequestDto), HttpStatus.OK);
	}
	
}
