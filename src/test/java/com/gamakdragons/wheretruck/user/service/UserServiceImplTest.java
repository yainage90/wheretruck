package com.gamakdragons.wheretruck.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.user.entity.User;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {UserServiceImpl.class, ElasticSearchServiceImpl.class, ElasticSearchConfig.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    
    @Value("${elasticsearch.index.user.name}")
    private String TEST_USER_INDEX_NAME;

    @Value("${elasticsearch.host}")
    private String ES_HOST;

    @Value("${elasticsearch.port}")
    private int ES_PORT;

    @Value("${elasticsearch.username}")
    private String ES_USER;

    @Value("${elasticsearch.password}")
    private String ES_PASSWORD;

    private RestHighLevelClient esClient;

    @BeforeEach
    public void beforeEach() throws IOException {
        initRestHighLevelClient();

        deleteTestUserIndex();
        createTestUserIndex();
        createTestUserData();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestUserIndex();
    }

    

    @Test
    void testGetById() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        User resultUser = userService.getById(user.getId());
        assertThat(resultUser, equalTo(user));

    }

    @Test
    void testSaveUser() {

        User user = createTestUserData();

        IndexResultDto indexResult = userService.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(user.getId()));

    }

    @Test
    void testUpdateUser() {

        User user = createTestUserData();
        indexTestUserData(user);

        String nickNameToUpdate = "updated " + user.getNickName();
        user.setNickName(nickNameToUpdate);

        UpdateResultDto updateResult = userService.updateUser(user);
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(userService.getById(user.getId()).getNickName(), equalTo(nickNameToUpdate));
    }


    @Test
    void testDeleteUser() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = userService.deleteUser(user.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        try {
            Thread.sleep(200);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(userService.getById(user.getId()), nullValue());
    }

    @Test
    void testAddFavorite() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<String> favoriteTruckIds = createTestFavoriteTruckIds();
        indexTestFavoriteTruckIds(user.getId(), favoriteTruckIds);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<String> savedTruckIds = userService.getById(user.getId()).getFavorites();
        assertThat(savedTruckIds, hasSize(10));
        assertThat(savedTruckIds, equalTo(favoriteTruckIds));
    }

    @Test
    void testDeleteFavorite() {

        User user = createTestUserData();
        indexTestUserData(user);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<String> favoriteTruckIds = createTestFavoriteTruckIds();
        indexTestFavoriteTruckIds(user.getId(), favoriteTruckIds);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 4; i++) {
            UpdateResultDto deleteResultDto = userService.deleteFavorite(user.getId()
                                            , favoriteTruckIds.remove(new Random().nextInt(favoriteTruckIds.size())));
            assertThat(deleteResultDto.getResult(), is("UPDATED"));
        }

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<String> remainedTruckIds = userService.getById(user.getId()).getFavorites();
        assertThat(remainedTruckIds, hasSize(6));
        assertThat(remainedTruckIds, is(favoriteTruckIds));
    }

    private void initRestHighLevelClient() {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ES_USER, ES_PASSWORD));

        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        )
        .setHttpClientConfigCallback((httpClientBuilder) -> {
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestUserIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_USER_INDEX_NAME);

        request.settings(Settings.builder()
            .put("index.number_of_shards", 3)
            .put("index.number_of_replicas", 1)
        );

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("id");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("nickName");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                
                builder.startObject("isOwner");
                {
                    builder.field("type", "boolean");
                }
                builder.endObject();

                builder.startObject("favorites");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();

        request.mapping(builder);

        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("index created: " + response.index());
        if(!response.isAcknowledged()) {
            throw new IOException();
        }

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private User createTestUserData() {

        return User.builder()
                    .id(UUID.randomUUID().toString())
                    .nickName("유저1")
                    .isOwner(false)
                    .build();
    }

    private void indexTestUserData(User user) {
        IndexResultDto indexResult = userService.saveUser(user);
        assertThat(indexResult.getId(), is(user.getId()));

        try {
            Thread.sleep(1500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }


    private List<String> createTestFavoriteTruckIds() {

        List<String> favoriteTruckIds = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            String truckId = UUID.randomUUID().toString();
            favoriteTruckIds.add(truckId);
        }

        

        return favoriteTruckIds;
    }

    private void indexTestFavoriteTruckIds(String userId, List<String> favoriteTruckIds) {
        favoriteTruckIds.forEach(truckId -> {
            UpdateResultDto updateResultDto = userService.addFavorite(userId, truckId);
            assertThat(updateResultDto.getResult(), is("UPDATED"));
        });
    }

    private void deleteTestUserIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_USER_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_USER_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }



}
