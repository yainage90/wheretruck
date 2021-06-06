package com.gamakdragons.wheretruck.domain.favorite.entity;

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
public class Favorite {
    
    private String id;
    private String truckId;
    private String userId;
}
