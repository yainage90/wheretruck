package com.gamakdragons.wheretruck.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LogoutResponseDto {

	private String id;
	private String result;
	
}
