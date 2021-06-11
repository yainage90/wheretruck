package com.gamakdragons.wheretruck.auth.dto;

import com.gamakdragons.wheretruck.domain.user.dto.Role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginRequestDto {
    
    private String accessToken;
    private String nickName;
    private Role role;
}
