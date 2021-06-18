package com.gamakdragons.wheretruck.domain.truck.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.domain.truck.service.TruckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/truck")
@Slf4j
public class TruckController {

    @Autowired
    private TruckService truckService;

    @GetMapping("/{id}")
    public ResponseEntity<Truck> getById(@PathVariable String id) {
        log.info("/truck/" + id);

        Truck truck = truckService.getById(id);

        return new ResponseEntity<>(truck, HttpStatus.OK);
    }

    /*@GetMapping("/favorite/{ids}")
    public ResponseEntity<SearchResultDto<Truck>> getByIds(@PathVariable List<String> ids) {
        log.info("/api/truck/" + ids);

        return new ResponseEntity<>(truckService.getByIds(ids), HttpStatus.OK);
    }*/

    @GetMapping("/all" )
    public ResponseEntity<SearchResultDto<Truck>> getAllTrucks() {
        log.info("/truck/all");

        return new ResponseEntity<>(truckService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/geo")
    public ResponseEntity<SearchResultDto<Truck>> getByGeoLocation(GeoLocation geoLocation, float distance) {
        log.info("/truck/geo. geoLocation=" + geoLocation + ", distance=" + distance);

        return new ResponseEntity<>(truckService.findByGeoLocation(geoLocation, distance), HttpStatus.OK);
    }

    @GetMapping("/my")
    public ResponseEntity<SearchResultDto<Truck>> my(HttpServletRequest request) {
        String userId = request.getAttribute("userId").toString();

        log.info("/truck/user. userId=" + userId);

        return new ResponseEntity<>(truckService.findByUserId(userId), HttpStatus.OK);
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<IndexUpdateResultDto> save(@RequestBody TruckSaveRequestDto truckSaveRequestDto, HttpServletRequest httpServletRequest) {
        log.info("/api/truck. truckSaveRequestDto=" + truckSaveRequestDto);

        truckSaveRequestDto.setUserId(httpServletRequest.getAttribute("userId").toString());

        IndexUpdateResultDto result;
        if(truckSaveRequestDto.getId() == null) {
            result = truckService.saveTruck(truckSaveRequestDto);
        } else {
            result = truckService.updateTruck(truckSaveRequestDto);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> delete(@PathVariable String id) {
        log.info("/truck/" + id);

        return new ResponseEntity<>(truckService.deleteTruck(id), HttpStatus.OK);
    }

    @PutMapping("/start/{truckId}")
    public ResponseEntity<IndexUpdateResultDto> startTruck(@PathVariable String truckId, @RequestBody GeoLocation geoLocation) {
        log.info("/truck/start. id=" + truckId + ", geoLocation=" + geoLocation);

        return new ResponseEntity<>(truckService.openTruck(truckId, geoLocation), HttpStatus.OK);
    }

    @PutMapping("/stop/{truckId}")
    public ResponseEntity<IndexUpdateResultDto> stopTruck(@PathVariable String truckId) {
        log.info("/truck/stop. id=" + truckId);

        return new ResponseEntity<>(truckService.stopTruck(truckId), HttpStatus.OK);
    }
}
