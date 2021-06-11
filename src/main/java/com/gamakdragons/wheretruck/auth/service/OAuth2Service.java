package com.gamakdragons.wheretruck.auth.service;

import com.gamakdragons.wheretruck.auth.dto.LoginRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LoginResponseDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutRequestDto;
import com.gamakdragons.wheretruck.auth.dto.LogoutResponseDto;

public interface OAuth2Service {
	
	LoginResponseDto login(LoginRequestDto loginRequestDto);
	LogoutResponseDto logout(String userId, LogoutRequestDto logoutRequestDto);
}
