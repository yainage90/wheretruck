package com.gamakdragons.wheretruck.user.entity;

import java.util.List;

import org.elasticsearch.client.security.user.privileges.Role;

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
    private List<String> favorites;
}
