package com.gamakdragons.wheretruck.rating.model;

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
public class Rating {

    private String id;
    private String userId;
    private String truckId;
    private int star;
    private String comment;
}
