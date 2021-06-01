package com.gamakdragons.wheretruck.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@Builder
@EqualsAndHashCode
public class GeoLocation {
    
    private float lon;
    private float lat;
}
