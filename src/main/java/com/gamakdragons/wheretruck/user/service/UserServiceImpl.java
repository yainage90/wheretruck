package com.gamakdragons.wheretruck.user.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.model.User;
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
public class UserServiceImpl implements UserService {

    @Value("${es.index.user.name}")
    private String USER_INDEX_NAME;

    @Value("${es.index.truck.name}")
    private String TRUCK_INDEX_NAME;

    @Value("${es.index.rating.name}")
    private String RATING_INDEX_NAME;

    private final ElasticSearchRestClient restClient;

    @Autowired
    public UserServiceImpl (ElasticSearchRestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public SearchResultDto<User> findAll() {

        SearchRequest request = EsRequestFactory.createSearchAllRequest(USER_INDEX_NAME); 

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
    public User getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(USER_INDEX_NAME, id);
        GetResponse getResponse;
        try {
            getResponse = restClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return new Gson().fromJson(getResponse.getSourceAsString(), User.class);
    }

    @Override
    public SearchResultDto<User> findByEmail(String email) {

        SearchRequest request = EsRequestFactory.createSearchByFieldRequest(USER_INDEX_NAME, "email", email);

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

    private SearchResultDto<User> makeSearhResultDtoFromSearchResponse(SearchResponse response) {
        return SearchResultDto.<User> builder()
                .status(response.status().name())
                .numFound(response.getHits().getTotalHits().value)
                .docs(
                    Arrays.stream(response.getHits().getHits())
                            .map(hit -> new Gson().fromJson(hit.getSourceAsString(), User.class))
                            .collect(Collectors.toList())
                ).build();
    }

    private SearchResultDto<User> makeErrorSearhResultDtoFromSearchResponse() {
        return SearchResultDto.<User> builder()
                .status(RestStatus.INTERNAL_SERVER_ERROR.name())
                .numFound(0)
                .docs(Collections.emptyList())
                .build();

    }

    @Override
    public IndexResultDto saveUser(User user) {

        IndexRequest request = EsRequestFactory.createIndexRequest(USER_INDEX_NAME, user.getId(), user);
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
                .id(response.getId())
                .build();

    }

    @Override
    public UpdateResultDto updateUser(User user) {

        UpdateRequest request = EsRequestFactory.createUpdateRequest(USER_INDEX_NAME, user.getId(), user);
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
    public DeleteResultDto deleteUser(String id) {

        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(USER_INDEX_NAME, id);
        DeleteResponse response;
        try {
            response = restClient.delete(request, RequestOptions.DEFAULT);
            try {
                deleteUserRelatedSources(new String[]{TRUCK_INDEX_NAME, RATING_INDEX_NAME}, id);
            } catch(ElasticsearchStatusException e) {
                log.error(e.getMessage());
            }
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

    private void deleteUserRelatedSources(String[] indices, String userId) {

        DeleteByQueryRequest request = EsRequestFactory.createDeleteByFieldRequest(indices, "userId", userId);
        BulkByScrollResponse response;
        try {
            response = restClient.deleteByQuery(request, RequestOptions.DEFAULT);
            log.info(response.getStatus().toString());
        } catch(IOException e) {
            log.error("IOException occured.");
        }
    }
}
