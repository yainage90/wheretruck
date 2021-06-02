package com.gamakdragons.wheretruck.favorite.service;

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
import com.gamakdragons.wheretruck.favorite.model.Favorite;

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

@SpringBootTest(classes = {FavoriteServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class FavoriteServiceImplTest {

    @Autowired
    private FavoriteService service;
    
    @Value("${es.index.favorite.name}")
    private String TEST_FAVORITE_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private Favorite favorite;

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
    void testDeleteFavorite() {

        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = service.deleteFavorite(favorite.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        assertThat(service.getById(favorite.getId()), nullValue());

    }

    @Test
    void testFindAll() {

        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getId(), is(favorite.getId()));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Favorite> result = service.findAll();
        log.info(result.toString());

        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));

        assertThat(result.getDocs(), hasItems(favorite));

    }

    @Test
    void testFindByTruckId() {
        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Favorite> resultFavorite = service.findByTruckId(favorite.getTruckId());
        log.info(resultFavorite.toString());

        assertThat(resultFavorite.getStatus(), equalTo("OK"));
        assertThat(resultFavorite.getNumFound(), equalTo(1L));
        assertThat(resultFavorite.getDocs().get(0), equalTo(favorite));

    }

    @Test
    void testFindByUserId() {

        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Favorite> resultFavorite = service.findByUserId(favorite.getUserId());
        log.info(resultFavorite.toString());

        assertThat(resultFavorite.getStatus(), equalTo("OK"));
        assertThat(resultFavorite.getNumFound(), equalTo(1L));
        assertThat(resultFavorite.getDocs().get(0), equalTo(favorite));

    }

    @Test
    void testGetById() {

        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Favorite resultFavorite = service.getById(favorite.getId());
        log.info(resultFavorite.toString());
        assertThat(resultFavorite, equalTo(favorite));

    }

    @Test
    void testSaveFavorite() {

        IndexResultDto indexResult = service.saveFavorite(favorite);
        log.info("favorite index result: " + indexResult.getResult() + ", favorite id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(favorite.getId()));

    }

    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestUserIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_FAVORITE_INDEX_NAME);

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

                builder.startObject("userId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("truckId");
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

        favorite = Favorite.builder()
                            .id(UUID.randomUUID().toString())
                            .userId("userid")
                            .truckId("truckid")
                            .build();

    }

    private void deleteTestUserIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_FAVORITE_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_FAVORITE_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

}
