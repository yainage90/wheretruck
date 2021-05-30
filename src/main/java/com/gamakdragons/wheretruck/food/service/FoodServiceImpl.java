package com.gamakdragons.wheretruck.food.service;

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
import com.gamakdragons.wheretruck.food.model.Food;
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
public class FoodServiceImpl implements FoodService {

    @Value("${es.index.food.name}")
    private String FOOD_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public FoodServiceImpl(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Food getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(FOOD_INDEX_NAME, id);
        GetResponse getResponse;
        try {
            getResponse = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return new Gson().fromJson(getResponse.getSourceAsString(), Food.class);
    }

    @Override
    public SearchResultDto<Food> findByTruckId(String truckId) {

        SearchRequest request = EsRequestFactory.createTruckIdSearchRequest(FOOD_INDEX_NAME, truckId);

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

    private SearchResultDto<Food> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<Food> builder()
                .status(response.status().name())
                .numFound(response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Food.class))
                            .collect(Collectors.toList())
                ).build();
    }

    private SearchResultDto<Food> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Food> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    @Override
    public IndexResultDto saveFood(Food food) {

        String id = UUID.randomUUID().toString();
        food.setId(id);

        IndexRequest request = EsRequestFactory.createIndexRequest(FOOD_INDEX_NAME, id, food);
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
    public UpdateResultDto updateFood(Food food) {

        UpdateRequest request = EsRequestFactory.createUpdateRequest(FOOD_INDEX_NAME, food.getId(), food);
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
    
    public DeleteResultDto deleteFood(String id) {

        DeleteRequest request = EsRequestFactory.creatDeleteRequest(FOOD_INDEX_NAME, id);
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
