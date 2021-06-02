package com.gamakdragons.wheretruck.truck.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.ElasticsearchStatusException;
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
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TruckServiceImpl implements TruckService {

    @Value("${es.index.truck.name}")
    private String TRUCK_INDEX_NAME;

    @Value("${es.index.food.name}")
    private String FOOD_INDEX_NAME;

    @Value("${es.index.rating.name}")
    private String RATING_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public TruckServiceImpl(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public SearchResultDto<Truck> findAll() {
        SearchRequest request = EsRequestFactory.createSearchAllRequest(TRUCK_INDEX_NAME); 

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
    public SearchResultDto<Truck> findByUserId(String userId) {
        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(TRUCK_INDEX_NAME, "userId", userId);

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
    public SearchResultDto<Truck> findByLocation(GeoLocation location, float distance) {

        SearchRequest request = EsRequestFactory.createGeoSearchRequest(TRUCK_INDEX_NAME, location, distance);
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

    private SearchResultDto<Truck> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<Truck> builder()
                .status(response.status().name())
                .numFound(response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Truck.class))
                            .collect(Collectors.toList())
                ).build();
    }

    private SearchResultDto<Truck> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Truck> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    @Override
    public Truck getById(String id) {
        GetRequest request = EsRequestFactory.createGetRequest(TRUCK_INDEX_NAME, id);
        GetResponse getResponse;
        try {
            getResponse = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        Truck truck = new Gson().fromJson(getResponse.getSourceAsString(), Truck.class);

        return truck;
    }

    @Override
    public IndexResultDto saveTruck(Truck truck) {

        String id = UUID.randomUUID().toString();
        truck.setId(id);

        IndexRequest request = EsRequestFactory.createIndexRequest(TRUCK_INDEX_NAME, id, truck);
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
    public UpdateResultDto updateTruck(Truck truck) {
        UpdateRequest request = EsRequestFactory.createUpdateRequest(TRUCK_INDEX_NAME, truck.getId(), truck);
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
    public DeleteResultDto deleteTruck(String id) {
        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(TRUCK_INDEX_NAME, id);
        DeleteResponse response;
        try {
            response = restClient.delete(request, RequestOptions.DEFAULT);
            try {
                deleteTruckRelatedSources(new String[]{FOOD_INDEX_NAME, RATING_INDEX_NAME}, id);
            } catch(ElasticsearchStatusException e) {
                log.error(e.getMessage());
            }
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

 
    @Override
    public UpdateResultDto openTruck(String id, GeoLocation location) {
        Truck truck = getById(id);
        truck.setOpened(true);
        truck.setGeoLocation(location);

        UpdateRequest request = EsRequestFactory.createUpdateRequest(TRUCK_INDEX_NAME, id, truck);
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
    public UpdateResultDto stopTruck(String id) {
        Truck truck = getById(id);
        truck.setOpened(false);

        UpdateRequest request = EsRequestFactory.createUpdateRequest(TRUCK_INDEX_NAME, id, truck);
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

    private void deleteTruckRelatedSources(String[] indices, String truckId) {

        DeleteByQueryRequest request = EsRequestFactory.createDeleteByFieldRequest(indices, "truckId", truckId);
        BulkByScrollResponse response;
        try {
            response = restClient.deleteByQuery(request, RequestOptions.DEFAULT);
            log.info(response.getStatus().toString());
        } catch(IOException e) {
            log.error("IOException occured.");
        }
    }

}
