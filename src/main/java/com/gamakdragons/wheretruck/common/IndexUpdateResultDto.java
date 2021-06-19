package com.gamakdragons.wheretruck.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class IndexUpdateResultDto {
    
    private String result;

    @JsonInclude(Include.NON_NULL)
    private String id;
}
