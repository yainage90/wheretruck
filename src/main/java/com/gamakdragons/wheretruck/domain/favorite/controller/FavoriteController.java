package com.gamakdragons.wheretruck.domain.favorite.controller;

import javax.servlet.http.HttpServletRequest;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/favorite")
@Slf4j
@RequiredArgsConstructor
public class FavoriteController {

	private final FavoriteService service;

	@GetMapping("/my")
    public ResponseEntity<SearchResultDto<Favorite>> getByUserId(HttpServletRequest httpServletRequest) {
        log.info("/api/favorite/my");

		String userId = httpServletRequest.getAttribute("userId").toString();

        return new ResponseEntity<>(service.findByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/truck/truckId")
    public ResponseEntity<SearchResultDto<Favorite>> getByTruckId(String truckId) {
        log.info("/favorite/search/truckId. truckId=" + truckId);

        return new ResponseEntity<>(service.findByTruckId(truckId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<IndexUpdateResultDto> save(@RequestBody Favorite favorite, HttpServletRequest httpServletRequest) {
        log.info("/favorite/save. favorite=" + favorite);

		String userId = httpServletRequest.getAttribute("userId").toString();
		favorite.setUserId(userId);

        return new ResponseEntity<>(service.saveFavorite(favorite), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResultDto> delete(@PathVariable String id) {
        log.info("/api/favorite/" + id + ". id=" + id);

        return new ResponseEntity<>(service.deleteFavorite(id), HttpStatus.OK);
    }
}