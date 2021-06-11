package com.gamakdragons.wheretruck.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogoutRequestDto {
	
	private String accessToken;
}
