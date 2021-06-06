package com.gamakdragons.wheretruck.domain.rating.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.gamakdragons.wheretruck.cloud.elasticsearch.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.domain.truck.service.TruckService;
import com.gamakdragons.wheretruck.domain.truck.service.TruckServiceImpl;

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

@SpringBootTest(classes = {RatingServiceImpl.class, TruckServiceImpl.class, ElasticSearchServiceImpl.class, ElasticSearchConfig.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class RatingServiceImplTest {

    @Autowired
    private TruckService truckService;

    @Autowired
    private RatingService ratingService;
    
    @Value("${elasticsearch.index.truck.name}")
    private String TEST_TRUCK_INDEX;

    @Value("${elasticsearch.host}")
    private String ES_HOST;

    @Value("${elasticsearch.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    @BeforeEach
    public void beforeEach() throws IOException, InterruptedException {
        initRestHighLevelClient();

        deleteTestTruckIndex();
        createTestTruckIndex();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestTruckIndex();
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

    private void createTestTruckIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_TRUCK_INDEX);

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

                builder.startObject("starAvg");
                {
                    builder.field("type", "float");
                }
                builder.endObject();

                builder.startObject("foods");
                {
                    builder.field("type", "nested");
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
                        builder.startObject("imageUrl");
                        {
                            builder.field("type", "keyword");
                        }
                        builder.endObject();
                    }
                    builder.endObject();
                }
                builder.endObject();
                
                builder.startObject("ratings");
                {
                    builder.field("type", "nested");
                    builder.startObject("properties");
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
                        builder.startObject("star");
                        {
                            builder.field("type", "double");
                        }
                        builder.endObject();
                        builder.startObject("comment");
                        {
                            builder.field("type", "keyword");
                        }
                        builder.endObject();
                        builder.startObject("createdDate");
                        {
                            builder.field("type", "date");
                            builder.field("format", "yyyy-MM-dd HH:mm:ss");
                        }
                        builder.endObject();
                        builder.startObject("updatedDate");
                        {
                            builder.field("type", "date");
                            builder.field("format", "yyyy-MM-dd HH:mm:ss");
                        }
                        builder.endObject();
                    builder.endObject();
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

    private void deleteTestTruckIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_TRUCK_INDEX);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_TRUCK_INDEX);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

    private List<Truck> createTestTruckData() {

        Truck truck1 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck1")
                            .geoLocation(GeoLocation.builder().lat(30).lon(130).build())
                            .description("this is truck1")
                            .opened(false)
                            .userId("userid1")
                            .build();

        Truck truck2 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck2")
                            .geoLocation(GeoLocation.builder().lat(40).lon(140).build())
                            .description("this is truck2")
                            .opened(true)
                            .userId("userid2")
                            .build();

        return Arrays.asList(truck1, truck2);
    }

    private void indexTestTruckData(List<Truck> trucks) {

        trucks.forEach(truck -> {
            IndexResultDto indexResult = truckService.saveTruck(truck);
            log.info("truck index result: " + indexResult.getResult() + ", truck id: " + indexResult.getId());
            assertThat(indexResult.getId(), is(truck.getId()));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<Rating> createTestRatingData() {

        Rating rating1 = Rating.builder()
                                .truckId(UUID.randomUUID().toString())
                                .userId(UUID.randomUUID().toString())
                                .comment("hello1")
                                .createdDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .updatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .build();

        Rating rating2 = Rating.builder()
                                .truckId(UUID.randomUUID().toString())
                                .userId(UUID.randomUUID().toString())
                                .comment("hello2")
                                .createdDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .updatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                                .build();
        return Arrays.asList(rating1, rating2);
    }

    private void indexTestRatingData(String truckId, List<Rating> ratings) {

        ratings.forEach(rating-> {
            UpdateResultDto updateResult = ratingService.saveRating(truckId, rating);
            log.info("rating index result: " + updateResult.getResult());
            assertThat(updateResult.getResult(), is("UPDATED"));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
