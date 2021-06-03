package com.gamakdragons.wheretruck.rating.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.rating.entity.Rating;
import com.gamakdragons.wheretruck.truck.entity.Truck;
import com.gamakdragons.wheretruck.truck.service.TruckService;
import com.gamakdragons.wheretruck.truck.service.TruckServiceImpl;

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

@SpringBootTest(classes = {RatingServiceImpl.class, TruckServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class RatingServiceImplTest {

    @Autowired
    private TruckService truckService;

    @Autowired
    private RatingService ratingService;
    
    @Value("${es.index.rating.name}")
    private String TEST_RATING_INDEX_NAME;

    @Value("${es.index.truck.name}")
    private String TEST_TRUCK_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private Rating rating;
    private Truck truck;

    @BeforeEach
    public void beforeEach() throws IOException, InterruptedException {
        initRestHighLevelClient();

        deleteTestTruckIndex();
        createTestTruckIndex();
        createAndIndexTestTruckData();

        deleteTestRatingIndex();
        createTestRatingIndex();
        createTestRatingData();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestRatingIndex();
        deleteTestTruckIndex();
    }

    @Test
    void testDeleteRating() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteResultDto deleteResult = ratingService.deleteRating(rating.getId());

        assertThat(deleteResult.getResult(), is("DELETED"));
        assertThat(ratingService.getById(rating.getId()), nullValue());
    }

    @Test
    void testFindByTruckId() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Rating> result = ratingService.findByTruckId("truckid");
        log.info(result.toString());

        assertThat(result.getStatus(), equalTo("OK"));
        assertThat(result.getNumFound(), equalTo(1));
        assertThat(result.getDocs().get(0), equalTo(rating));

    }

    @Test
    void testFindByUserId() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Rating> result = ratingService.findByUserId("userid");
        log.info(result.toString());

        assertThat(result.getStatus(), equalTo("OK"));
        assertThat(result.getNumFound(), equalTo(1));
        assertThat(result.getDocs().get(0), equalTo(rating));

    }

    @Test
    void testGetById() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Rating resultRating = ratingService.getById(rating.getId());
        log.info(resultRating.toString());
        assertThat(resultRating, equalTo(rating));

    }

    @Test
    void testSaveRating() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));
        assertThat(indexResult.getId(), is(rating.getId()));

    }

    @Test
    void testUpdateRating() {

        IndexResultDto indexResult = ratingService.saveRating(rating);
        log.info("rating index result: " + indexResult.getResult() + ", rating id: " + indexResult.getId());

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String commentToUpdate = "정말 재밌어요ㅋㅋ";
        rating.setComment(commentToUpdate);
        UpdateResultDto updateResult = ratingService.updateRating(rating);

        assertThat(updateResult.getResult(), is("UPDATED"));
        assertThat(ratingService.getById(rating.getId()).getComment(), equalTo(commentToUpdate));

    }

    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestRatingIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_RATING_INDEX_NAME);

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

                builder.startObject("star");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();

                builder.startObject("comment");
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
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createTestRatingData() throws InterruptedException {

        rating = Rating.builder()
                            .id(UUID.randomUUID().toString())
                            .star(5)
                            .comment("맛있어요")
                            .truckId(truck.getId())
                            .userId("userid")
                            .build();

    }

    private void deleteTestRatingIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_RATING_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_RATING_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

    private void createTestTruckIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_TRUCK_INDEX_NAME);

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

                builder.startObject("geoLocation");
                {
                    builder.field("type", "geo_point");
                }
                builder.endObject();

                builder.startObject("description");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("opened");
                {
                    builder.field("type", "boolean");
                }
                builder.endObject();

                builder.startObject("userId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                
                builder.startObject("numRating");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();

                builder.startObject("score");
                {
                    builder.field("type", "float");
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

    private void createAndIndexTestTruckData() throws InterruptedException {

        truck = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck")
                            .geoLocation(GeoLocation.builder().lat(30).lon(130).build())
                            .description("this is truck")
                            .opened(false)
                            .userId("1")
                            .build();
        
        truckService.saveTruck(truck);

        Thread.sleep(1000);
        
    }

    private void deleteTestTruckIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_TRUCK_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_TRUCK_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

}
