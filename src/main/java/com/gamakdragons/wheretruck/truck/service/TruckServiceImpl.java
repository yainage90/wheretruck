package com.gamakdragons.wheretruck.truck.service;

import java.io.IOException;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.foodtruck_region.model.GeoLocation;
import com.gamakdragons.wheretruck.truck.model.Truck;
import com.gamakdragons.wheretruck.truck.model.TruckIndexResponse;
import com.gamakdragons.wheretruck.truck.model.TruckSearchResponse;
import com.gamakdragons.wheretruck.util.ElasticSearchUtil;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TruckServiceImpl implements TruckService {

    @Value("${es.index.truck.name}")
    private String TRUCK_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public TruckServiceImpl(ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    public TruckSearchResponse findAll() {
        SearchRequest request = EsRequestFactory.createSearchAllRequest(TRUCK_INDEX_NAME); 

        SearchResponse searchResponse;
        try {
            searchResponse = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + searchResponse.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        TruckSearchResponse response = ElasticSearchUtil.createTruckResponseFromSearchResponse(searchResponse);

        return response;

    }

    @Override
    public Truck findById(String id) {
        GetRequest request = EsRequestFactory.createGetByIdRequest(TRUCK_INDEX_NAME, id);
        GetResponse getResponse;
        try {
            getResponse = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return new Gson().fromJson(getResponse.getSourceAsString(), Truck.class);
    }

    @Override
    public TruckSearchResponse findByLocation(GeoLocation location, float distance) {

        SearchRequest request = EsRequestFactory.createGeoSearchRequest(TRUCK_INDEX_NAME, location, distance);
        SearchResponse searchResponse;
        try {
            searchResponse = restClient.search(request, RequestOptions.DEFAULT);
            log.info("total hits: " + searchResponse.getHits().getTotalHits());
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        TruckSearchResponse response = ElasticSearchUtil.createTruckResponseFromSearchResponse(searchResponse);

        return response;
    }

    @Override
    public TruckIndexResponse registerTruck(Truck truck) {
        IndexRequest request = EsRequestFactory.createTruckIndexRequest(TRUCK_INDEX_NAME, truck);
        IndexResponse indexResponse;
        try {
            indexResponse = restClient.index(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return TruckIndexResponse.builder()
                .result(indexResponse.getResult().name())
                .id(indexResponse.getId())
                .build();
    }

    @Override
    public TruckSearchResponse updateTruck(Truck truck) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public TruckSearchResponse deleteTruck(String id) {
        // TODO Auto-generated method stub
        return null;
    }

 
    @Override
    public Truck openTruck(String id, GeoLocation location) {
        return null;
    }

    @Override
    public Truck stopTruck(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    

}
