package com.gamakdragons.wheretruck.user.model;

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
    private String email;
    private String name;
    private String nickName;
}
