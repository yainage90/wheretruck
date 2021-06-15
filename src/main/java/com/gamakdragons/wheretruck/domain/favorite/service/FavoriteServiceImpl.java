package com.gamakdragons.wheretruck.domain.favorite.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
	
	@Value("${elasticsearch.index.favorite.name}")
    private String FAVORITE_INDEX_NAME;

    private final RestHighLevelClient esClient;

	@Override
    public SearchResultDto<Favorite> findByTruckId(String truckId) {

        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(FAVORITE_INDEX_NAME, "truckId", truckId);

        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);

    }

    @Override
    public SearchResultDto<Favorite> findByUserId(String userId) {

        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(FAVORITE_INDEX_NAME, "userId", userId);

        SearchResponse response;
        try {
            response = esClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + response.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return makeErrorSearhResultDtoFromSearchResponse();
        }

        return makeSearhResultDtoFromSearchResponse(response);

    }

    private SearchResultDto<Favorite> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<Favorite> builder()
                .status(response.status().name())
                .numFound((int) response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Favorite.class))
                            .collect(Collectors.toList())
                ).build();
    }

    private SearchResultDto<Favorite> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Favorite> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    @Override
    public IndexUpdateResultDto saveFavorite(Favorite favorite) {

        favorite.setId(UUID.randomUUID().toString());

        IndexRequest request = EsRequestFactory.createIndexRequest(FAVORITE_INDEX_NAME, favorite.getId(), favorite);
        IndexResponse response;
        try {
            response = esClient.index(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return IndexUpdateResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();

        }

        return IndexUpdateResultDto.builder()
                .result(response.getResult().name())
                .id(response.getId())
                .build();

    }

    @Override
    public DeleteResultDto deleteFavorite(String id) {

        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(FAVORITE_INDEX_NAME, id);
        DeleteResponse response;
        try {
            response = esClient.delete(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error(e.getMessage());
            return DeleteResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        } 

        return DeleteResultDto.builder()
                .result(response.getResult().name())
                .build();

    }
}
