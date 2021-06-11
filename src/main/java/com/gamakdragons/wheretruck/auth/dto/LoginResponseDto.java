package com.gamakdragons.wheretruck.auth.dto;

import com.gamakdragons.wheretruck.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class LoginResponseDto {
	
	User user;
	String jwt;
}
