package com.gamakdragons.wheretruck.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResultDto<T> {
    
    private long numFound;
    private List<T> results;
}
