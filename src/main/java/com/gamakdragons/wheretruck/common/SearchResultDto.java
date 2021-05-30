package com.gamakdragons.wheretruck.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResultDto<T> {
    
    private String status;
    private long numFound;
    private List<T> docs;
}
