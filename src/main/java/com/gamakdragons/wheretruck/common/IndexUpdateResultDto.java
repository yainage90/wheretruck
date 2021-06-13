package com.gamakdragons.wheretruck.common;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class IndexUpdateResultDto {
    
    private String result;
    private String id;
}
