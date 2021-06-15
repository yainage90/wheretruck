package com.gamakdragons.wheretruck.domain.user.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.dto.Role;
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
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
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

    @Value("${elasticsearch.index.favorite.name}")
    private String FAVORITE_INDEX;

    private final RestHighLevelClient esClient;

    @Override
    public User getById(String id) {

        GetRequest request = EsRequestFactory.createGetRequest(USER_INDEX, id);
        GetResponse getResponse;
        try {
            getResponse = esClient.get(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
            return null;
        }

        return new Gson().fromJson(getResponse.getSourceAsString(), User.class);
    }

    @Override
    public IndexUpdateResultDto saveUser(User user) {

        IndexRequest request = EsRequestFactory.createIndexRequest(USER_INDEX, user.getId(), user);
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
    public UpdateResultDto changeNickName(String userId, String nickName) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", userId);
        params.put("nickName", nickName);
        
        String script = "ctx._source.nickName = params.nickName";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(USER_INDEX, userId, inline);
        UpdateResponse response;
        try {
            response = esClient.update(request, RequestOptions.DEFAULT);
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
    public UpdateResultDto changeRole(String userId, Role role) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", userId);
        params.put("role", role.name());
        
        String script = "ctx._source.role = params.role";
        Script inline = new Script(ScriptType.INLINE, "painless", script, params);

        UpdateRequest request = EsRequestFactory.createUpdateWithScriptRequest(USER_INDEX, userId, inline);
        UpdateResponse response;
        try {
            response = esClient.update(request, RequestOptions.DEFAULT);
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
            response = esClient.delete(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error(e.getMessage());
            return DeleteResultDto.builder()
                    .result(e.getLocalizedMessage())
                    .build();
        } 

        deleteRelatedFavorites(id);

        return DeleteResultDto.builder()
                .result(response.getResult().name())
                .build();

    }

    private void deleteRelatedFavorites(String userId) {

        DeleteByQueryRequest request = EsRequestFactory.createDeleteByQuerydRequest(new String[]{FAVORITE_INDEX}, "userId", userId);

        try {
            esClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch(IOException e) {
            log.error("IOException occured.");
        }
    }

}
