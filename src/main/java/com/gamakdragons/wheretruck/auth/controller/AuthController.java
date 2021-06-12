package com.gamakdragons.wheretruck.auth.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.auth.dto.LoginRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LoginResponseDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutResponseDto;
import com.gamakdragons.wheretruck.auth.service.OAuth2Service;

import org.springframework.beans.factory.annotation.Qualifier;
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

	
	@Qualifier("kakao")
	private final OAuth2Service kakaoAuthService;
	
	@Qualifier("apple")
	private final OAuth2Service appleAuthService;

	@PutMapping("/login/kakao")
	public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestBody LoginRequestDto loginRequestDto) {
		log.info("/login. loginRequestDto=" + loginRequestDto);

		return new ResponseEntity<>(kakaoAuthService.login(loginRequestDto), HttpStatus.OK);
	}

	@PutMapping("/logout/kakao")
	public ResponseEntity<LogoutResponseDto> kakaoLogout(@RequestBody LogoutRequestDto logoutRequestDto, HttpServletRequest httpServletRequest) {
		String userId = httpServletRequest.getParameter("userId");

		log.info("/logout. logoutRequestDto=" + logoutRequestDto + ", userId=" + userId);

		return new ResponseEntity<>(kakaoAuthService.logout(userId, logoutRequestDto), HttpStatus.OK);
	}
	
	@PutMapping("/login/apple")
	public ResponseEntity<LoginResponseDto> appleLogin(@RequestBody LoginRequestDto loginRequestDto) {
		log.info("/login. loginRequestDto=" + loginRequestDto);

		return new ResponseEntity<>(appleAuthService.login(loginRequestDto), HttpStatus.OK);
	}

	@PutMapping("/logout/apple")
	public ResponseEntity<LogoutResponseDto> appleLogout(@RequestBody LogoutRequestDto logoutRequestDto, HttpServletRequest httpServletRequest) {
		String userId = httpServletRequest.getParameter("userId");

		log.info("/logout. logoutRequestDto=" + logoutRequestDto + ", userId=" + userId);

		return new ResponseEntity<>(appleAuthService.logout(userId, logoutRequestDto), HttpStatus.OK);
	}
}
