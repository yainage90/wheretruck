package com.gamakdragons.wheretruck.cloud.aws.entity;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class S3Status {
    
    private String regionName;
    private String accountOwner;
    private List<String> bucketList;
}
