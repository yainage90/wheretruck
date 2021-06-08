package com.gamakdragons.wheretruck.domain.rating.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class MyRatingDto {

    private String truckId;
    private String truckName;

    private String id;
    private String userId;
    private float star;
    private String comment;
    private String createdDate;
    private String updatedDate;
}
