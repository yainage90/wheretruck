package com.gamakdragons.wheretruck.foodtruck_region.model;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FoodTruckRegion {
    
    private String regionName;
    private int regionType;
    private String city;
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
