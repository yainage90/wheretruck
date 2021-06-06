package com.gamakdragons.wheretruck.domain.region.entity;

import com.gamakdragons.wheretruck.common.GeoLocation;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@EqualsAndHashCode
public class Region {
    
    private String regionName;
    private int regionType;
    private String city;
    private String town;
    private String roadAddress;
    private String postAddress;
    private GeoLocation geoLocation;
    private int capacity;
    private String cost;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String permissionStartDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String permissionEndDate;
    private String closedDays;
    private String weekdayStartTime;
    private String weekdayEndTime;
    private String weekendStartTime;
    private String weekendEndTime;
    private String restrictedItems;
    private String agencyName;
    private String agencyTel;
}
