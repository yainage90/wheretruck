package com.gamakdragons.wheretruck.domain.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    USER("ROLE_USER"),
    OWNER("ROLE_OWNER");

    private final String key;
}
