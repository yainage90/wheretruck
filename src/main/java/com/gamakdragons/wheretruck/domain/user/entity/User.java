package com.gamakdragons.wheretruck.domain.user.entity;

import com.gamakdragons.wheretruck.domain.user.dto.Role;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class User {
    
    private String id;
    private String nickName;
    private Role role;
}

