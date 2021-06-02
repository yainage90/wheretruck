package com.gamakdragons.wheretruck.rating.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.model.Rating;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RatingServiceImpl implements RatingService {
    
    @Value("${es.index.rating.name}")
    private String RATING_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public RatingServiceImpl (ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Rating getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(RATING_INDEX_NAME, id);
        GetResponse getResponse;
        try {
            getResponse = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return new Gson().fromJson(getResponse.getSourceAsString(), Rating.class);
    }

    @Override
    public SearchResultDto<Rating> findByTruckId(String truckId) {

        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(RATING_INDEX_NAME, "truckId", truckId);

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

    private SearchResultDto<Rating> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<Rating> builder()
                .status(response.status().name())
                .numFound(response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Rating.class))
                            .collect(Collectors.toList())
                ).build();
    }

    private SearchResultDto<Rating> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Rating> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    @Override
    public SearchResultDto<Rating> findByUserId(String userId) {

        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(RATING_INDEX_NAME, "userId", userId);

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
    public IndexResultDto saveRating(Rating rating) {

        String id = UUID.randomUUID().toString();
        rating.setId(id);

        IndexRequest request = EsRequestFactory.createIndexRequest(RATING_INDEX_NAME, id, rating);
        IndexResponse response;
        try {
            response = restClient.index(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return IndexResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();

        }

        return IndexResultDto.builder()
                .result(response.getResult().name())
                .id(id)
                .build();
    }

    @Override
    public UpdateResultDto updateRating(Rating rating) {

        UpdateRequest request = EsRequestFactory.createUpdateRequest(RATING_INDEX_NAME, rating.getId(), rating);
        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return UpdateResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();
        }

        return UpdateResultDto.builder()
                .result(response.getResult().name())
                .build();

    }

    @Override
    public DeleteResultDto deleteRating(String id) {

        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(RATING_INDEX_NAME, id);
        DeleteResponse response;
        try {
            response = restClient.delete(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return DeleteResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        }

        return DeleteResultDto.builder()
                .result(response.getResult().name())
                .build();
    }

}
