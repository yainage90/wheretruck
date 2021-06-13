package com.gamakdragons.wheretruck.domain.truck.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.cloud.aws.exception.S3ServiceException;
import com.gamakdragons.wheretruck.cloud.aws.service.S3Service;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TruckServiceImpl implements TruckService {

    @Value("${elasticsearch.index.truck.name}")
    private String TRUCK_INDEX;

    @Value("${elasticsearch.index.favorite.name}")
    private String FAVORITE_INDEX;

    @Value("${cloud.aws.s3.bucket.truck_image}")
    private String TRUCK_IMAGE_BUCKET;

    @Value("${cloud.aws.s3.bucket.food_image}")
    private String FOOD_IMAGE_BUCKET;

    private final ElasticSearchServiceImpl restClient;
    private final S3Service s3Service;


    @Autowired
    public TruckServiceImpl(ElasticSearchServiceImpl restClient, S3Service s3Service) {
        this.restClient = restClient;
        this.s3Service = s3Service;
    }

    @Override
    public Truck getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(TRUCK_INDEX, id);
        GetResponse response;
        try {
            response = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        if(!response.isExists()) {
            return null;
        }

        Truck truck = new Gson().fromJson(response.getSourceAsString(), Truck.class);
        truck.setRatings(truck.getRatings().stream().sorted((r1, r2) -> r2.getCreatedDate().compareTo(r1.getCreatedDate())).collect(Collectors.toList()));

        return truck;
    }

    @Override
    public SearchResultDto<Truck> getByIds(List<String> ids) {

        MultiGetRequest request = EsRequestFactory.createMultiGetRequest(TRUCK_INDEX, ids);
        MultiGetResponse response;
        try {
            response = restClient.multiGet(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("IOException occured.");
            return null;
        }

        List<Truck> trucks = Arrays.stream(response.getResponses())
                                    .map(
                                        item -> 
                                            new Truck(
                                                item.getResponse().getField("id").getValue(),
                                                item.getResponse().getField("name").getValue(),
                                                null,
                                                null,
                                                item.getResponse().getField("opened").getValue(),
                                                null,
                                                item.getResponse().getField("numRating").getValue(),
                                                item.getResponse().getField("starAvg").getValue(),
                                                item.getResponse().getField("getImageUrl").getValue(),
                                                null,
                                                null
                                            )
                                    )
                                    .collect(Collectors.toList());

        return SearchResultDto.<Truck> builder()
                .status("OK")
                .numFound(trucks.size())
                .docs(trucks)
                .build();
    }
    

    @Override
    public SearchResultDto<Truck> findAll() {

        String[] fieldsToInclude = new String[]{};
        String[] fieldsToExclude = new String[]{"foods", "ratings"};
        SearchRequest request = EsRequestFactory.createSearchAllRequest(TRUCK_INDEX, fieldsToInclude, fieldsToExclude); 

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
        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(TRUCK_INDEX, "userId", userId);

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
    public SearchResultDto<Truck> findByGeoLocation(GeoLocation geoLocation, float distance) {

        String[] fieldsToInclude = new String[]{};
        String[] fieldsToExclude = new String[]{"foods", "ratings"};
        SearchRequest request = EsRequestFactory.createGeoSearchRequest(TRUCK_INDEX, geoLocation, distance, fieldsToInclude, fieldsToExclude);
        
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
        List<Truck> trucks = Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), Truck.class))
                            .collect(Collectors.toList());
        
        trucks.stream().filter(truck -> truck.getRatings() != null).forEach(truck -> {
            truck.setRatings(truck.getRatings().stream().sorted((r1, r2) -> r2.getCreatedDate().compareTo(r1.getCreatedDate())).collect(Collectors.toList()));
        });

        return SearchResultDto.<Truck> builder()
                .status(response.status().name())
                .numFound((int) response.getHits().getTotalHits().value)
                .docs(trucks).build();
    }

    private SearchResultDto<Truck> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<Truck> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    

    @Override
    public IndexUpdateResultDto saveTruck(TruckSaveRequestDto truckSaveRequestDto) {

        Truck truck = truckSaveRequestDto.toSaveEntity();
        truck.setGeoLocation(new GeoLocation(0.0f, 0.0f));

        if(truckSaveRequestDto.getImage() != null) {
            try {
                String truckImageUrl = s3Service.uploadImage(TRUCK_IMAGE_BUCKET, truck.getId(), truckSaveRequestDto.getImage());
                log.info("image uploaded to s3 bucket. url=" + truckImageUrl);
                truck.setImageUrl(truckImageUrl);
            } catch(S3ServiceException e) {
                log.error(e.getMessage());
            }
            
        }

        log.info(truck.toString());

        IndexRequest request = EsRequestFactory.createIndexRequest(TRUCK_INDEX, truck.getId(), truck);
        IndexResponse response;
        try {
            response = restClient.index(request, RequestOptions.DEFAULT);
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
    public IndexUpdateResultDto updateTruck(TruckSaveRequestDto truckSaveRequestDto) {

        Truck truck = truckSaveRequestDto.toUpdateEntity();

        if(truckSaveRequestDto.getImage() != null) {
            String truckImageUrl = s3Service.uploadImage(TRUCK_IMAGE_BUCKET, truck.getId(), truckSaveRequestDto.getImage());
            log.info("image uploaded to s3 bucket. url=" + truckImageUrl);
            truck.setImageUrl(truckImageUrl);
        } else {
            if(s3Service.deleteImage(TRUCK_IMAGE_BUCKET, truck.getId())) {
                log.info("truck image removed.");
            }
        }

        log.info(truck.toString());

        String script = "ctx._source.name = params.name;" +
                        "ctx._source.description = params.description;" +
                        "ctx._source.imageUrl = params.imageUrl;";

        Map<String, Object> params = new HashMap<>();
        params.put("name", truck.getName());
        params.put("description", truck.getDescription());
        params.put("imageUrl", truck.getImageUrl());

        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, truck.getId(), inline);
        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return IndexUpdateResultDto.builder()
                .result(e.getLocalizedMessage())
                .build();
        }

        return IndexUpdateResultDto.builder()
                .result(response.getResult().name())
                .id(truck.getId())
                .build();
    }

    @Override
    public DeleteResultDto deleteTruck(String id) {
        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(TRUCK_INDEX, id);
        DeleteResponse response;

        try {
            response = restClient.delete(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return DeleteResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        }

        deleteFavorites(id);

        try {
            s3Service.deleteImage(TRUCK_IMAGE_BUCKET, id);
            s3Service.deleteImagesWithPrefix(FOOD_IMAGE_BUCKET, id);
        } catch(S3ServiceException e) {
            log.error(e.getMessage());
        }

        return DeleteResultDto.builder()
                .result(response.getResult().name())
                .build();
    }

    private void deleteFavorites(String truckId) {

        DeleteByQueryRequest request = EsRequestFactory.createDeleteByQuerydRequest(new String[]{FAVORITE_INDEX}, "truckId", truckId);

        try {
            restClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
        }
    }

    @Override
    public IndexUpdateResultDto openTruck(String id, GeoLocation geoLocation) {

        Map<String, Object> params = new HashMap<>();
        params.put("opened", true);
        params.put("lat", geoLocation.getLat());
        params.put("lon", geoLocation.getLon());
        String script = "ctx._source.opened=params.opened;" +
                        "ctx._source.geoLocation.lat=params.lat;" +
                        "ctx._source.geoLocation.lon=params.lon;";

        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, id, inline);

        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return IndexUpdateResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        }

        return IndexUpdateResultDto.builder()
                .result(response.getResult().name())
                .build();
    }

    @Override
    public IndexUpdateResultDto stopTruck(String id) {

        Map<String, Object> params = new HashMap<>();
        params.put("opened", false);
        String script = "ctx._source.opened=params.opened;";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(TRUCK_INDEX, id, inline);
        
        UpdateResponse response;
        try {
            response = restClient.update(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return IndexUpdateResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        }

        return IndexUpdateResultDto.builder()
                .result(response.getResult().name())
                .build();
    }

}
