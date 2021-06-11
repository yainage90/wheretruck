package com.gamakdragons.wheretruck.auth.dto;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class LogoutResponseDto {

	private String id;
	private String result;
	
}
