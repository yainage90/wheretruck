package com.gamakdragons.wheretruck.auth.dto;

import com.gamakdragons.wheretruck.domain.user.entity.User;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class LoginResponseDto {
	
	User user;
	String jwt;
}
