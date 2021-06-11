package com.gamakdragons.wheretruck.domain.user.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${elasticsearch.index.user.name}")
    private String USER_INDEX;

    private final ElasticSearchServiceImpl restClient;

    @Override
    public User getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(USER_INDEX, id);
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
    public IndexResultDto saveUser(User user) {

        user.setFavorites(Collections.emptyList());

        IndexRequest request = EsRequestFactory.createIndexRequest(USER_INDEX, user.getId(), user);
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
    public UpdateResultDto changeNickName(String userId, String nickName) {

        User userToUpdate = User.builder()
                                .id(userId)
                                .nickName(nickName)
                                .build();

        UpdateRequest request = EsRequestFactory.createUpdateRequest(USER_INDEX, userId, userToUpdate);
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

        DeleteRequest request = EsRequestFactory.createDeleteByIdRequest(USER_INDEX, id);
        DeleteResponse response;
        try {
            response = restClient.delete(request, RequestOptions.DEFAULT);
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


    @Override
    public UpdateResultDto addFavorite(String userId, String truckId) {

        Map<String, Object> params = new HashMap<>();
        params.put("truckId", truckId);

        String script = "if(ctx._source.favorites == null) {ctx._source.favorites = new ArrayList();}" + 
                        "ctx._source.favorites.add(params.truckId);";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(USER_INDEX, userId, inline);

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
    public UpdateResultDto deleteFavorite(String userId, String truckId) {

        Map<String, Object> params = new HashMap<>();
        params.put("truckId", truckId);

        String script = "ctx._source.favorites.removeIf(truckId -> truckId == params.truckId);";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(USER_INDEX, userId, inline);
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

}
