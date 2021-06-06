package com.gamakdragons.wheretruck.domain.region.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.region.entity.Region;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RegionServiceImpl implements RegionService {
    
    @Value("${elasticsearch.index.region.name}")
    private String FOOD_TRUCK_REGION_INDEX_NAME;

    private final ElasticSearchServiceImpl restClient;

    @Autowired
    public RegionServiceImpl(ElasticSearchServiceImpl restClient) {
        this.restClient = restClient;
    }

    @Override
    public SearchResultDto<Region> findAll() {

        SearchRequest request = EsRequestFactory.createSearchAllRequest(FOOD_TRUCK_REGION_INDEX_NAME); 

        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);
    }

    @Override
    public SearchResultDto<Region> findByAddress(String city, String town) {

        SearchRequest request = EsRequestFactory.createAddressSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME ,city, town);
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);
    }

    @Override
    public SearchResultDto<Region> findByLocation(GeoLocation geoLocation, float distance) {

        SearchRequest request = EsRequestFactory.createGeoSearchRequest(FOOD_TRUCK_REGION_INDEX_NAME, geoLocation, distance);
        SearchResponse response;
        try {
            response = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }


        return makeSearhResultDtoFromSearchResponse(response);
    }

    private SearchResultDto<Region> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<Region> builder()
                .status(response.status().name())
                .numFound((int) response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Region.class))
                            .collect(Collectors.toList())
                ).build();

    }

    private SearchResultDto<Region> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Region> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }
}
