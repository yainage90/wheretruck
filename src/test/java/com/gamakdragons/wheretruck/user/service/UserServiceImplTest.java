package com.gamakdragons.wheretruck.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.entity.User;

import org.apache.http.HttpHost;
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

@SpringBootTest(classes = {UserServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class UserServiceImplTest {

    @Autowired
    private UserService service;
    
    @Value("${es.index.user.name}")
    private String TEST_USER_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private User user;

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
    void testFindAll() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getId(), is(user.getId()));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<User> result = service.findAll();
        log.info(result.toString());

        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));

        assertThat(result.getDocs(), hasItems(user));

    }

    @Test
    void testDeleteUser() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = service.deleteUser(user.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        assertThat(service.getById(user.getId()), nullValue());

    }

    @Test
    void testFindByEmail() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<User> resultUser = service.findByEmail(user.getEmail());
        log.info(resultUser.toString());

        assertThat(resultUser.getStatus(), equalTo("OK"));
        assertThat(resultUser.getNumFound(), equalTo(1L));
        assertThat(resultUser.getDocs().get(0), equalTo(user));

    }

    @Test
    void testGetById() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        User resultUser = service.getById(user.getId());
        log.info(resultUser.toString());
        assertThat(resultUser, equalTo(user));

    }

    @Test
    void testSaveUser() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(user.getId()));

    }

    @Test
    void testUpdateUser() {

        IndexResultDto indexResult = service.saveUser(user);
        log.info("user index result: " + indexResult.getResult() + ", user id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String emailToUpdate = "goodbye@xyz.net";
        user.setEmail(emailToUpdate);
        UpdateResultDto updateResult = service.updateUser(user);

        assertThat(updateResult.getResult(), is("UPDATED"));
        assertThat(service.getById(user.getId()).getEmail(), equalTo(emailToUpdate));

    }

    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

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

                builder.startObject("email");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("name");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("nickName");
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

    private void createTestUserData() {

        user = User.builder()
                            .id(UUID.randomUUID().toString())
                            .email("hello@xyz.com")
                            .name("두한이")
                            .nickName("잇뽕")
                            .build();

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
