package com.gamakdragons.wheretruck.domain.rating.entity;

import java.util.HashMap;
import java.util.Map;

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
    private float star;
    private String comment;
    private String createdDate;
    private String updatedDate;

    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();
        map.put("id", getId());
        map.put("userId", getUserId());
        map.put("star", getStar());
        map.put("comment", getComment());
        map.put("createdDate", getCreatedDate());
        map.put("updatedDate", getUpdatedDate());

        return map;
    }
}
