package com.gamakdragons.wheretruck.food.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.food.entity.Food;

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

@SpringBootTest(classes = {FoodServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class FoodServiceImplTest {

    @Autowired
    private FoodService service;
    
    @Value("${es.index.food.name}")
    private String TEST_FOOD_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private Food food;

    @BeforeEach
    public void beforeEach() throws IOException {
        initRestHighLevelClient();
        deleteTestIndex();
        createTestTruckIndex();
        createTestData();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestIndex();
    }


    @Test
    void testDeleteFood() {

        IndexResultDto indexResult = service.saveFood(food);
        log.info("food index result: " + indexResult.getResult() + ", food id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = service.deleteFood(food.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        assertThat(service.getById(food.getId()), nullValue());
    }

    @Test
    void testFindByTruckId() {

        IndexResultDto indexResult = service.saveFood(food);
        log.info("food index result: " + indexResult.getResult() + ", food id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Food> resultFood = service.findByTruckId("truckid");
        log.info(resultFood.toString());

        assertThat(resultFood.getStatus(), equalTo("OK"));
        assertThat(resultFood.getNumFound(), equalTo(1L));
        assertThat(resultFood.getDocs().get(0), equalTo(food));

    }

    @Test
    void testGetById() {

        IndexResultDto indexResult = service.saveFood(food);
        log.info("food index result: " + indexResult.getResult() + ", food id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Food resultFood = service.getById(food.getId());
        log.info(resultFood.toString());
        assertThat(resultFood, equalTo(food));
    }

    @Test
    void testSaveFood() {

        IndexResultDto indexResult = service.saveFood(food);
        log.info("food index result: " + indexResult.getResult() + ", food id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(food.getId()));

    }

    @Test
    void testUpdateFood() {

        IndexResultDto indexResult = service.saveFood(food);
        log.info("food index result: " + indexResult.getResult() + ", food id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String descriptionToUpdate = "this is updated hotdog";
        food.setDescription(descriptionToUpdate);
        UpdateResultDto updateResult = service.updateFood(food);

        assertThat(updateResult.getResult(), is("UPDATED"));
        assertThat(service.getById(food.getId()).getDescription(), equalTo(descriptionToUpdate));
    }


    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestTruckIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_FOOD_INDEX_NAME);

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

                builder.startObject("name");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("cost");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();

                builder.startObject("description");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("truckId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("image");
                {
                    builder.field("type", "dense_vector");
                    builder.field("dims", 128);
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

    private void createTestData() {

        food = Food.builder()
                            .id(UUID.randomUUID().toString())
                            .name("hotdog")
                            .cost(5000)
                            .description("this is hotdog")
                            .image(new byte[128])
                            .truckId("truckid")
                            .build();

    }

    private void deleteTestIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_FOOD_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_FOOD_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

}
