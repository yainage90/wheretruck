package com.gamakdragons.wheretruck.common;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class SearchResultDto<T> {
    
    private String status;
    private int numFound;
    private List<T> docs;
}
