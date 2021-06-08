package com.gamakdragons.wheretruck.domain.rating.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
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

    @Value("${elasticsearch.username}")
    private String ES_USER;

    @Value("${elasticsearch.password}")
    private String ES_PASSWORD;

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
    void testSaveRatingUpdateTruckNumRatingAndStarAvg() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();

        ratings.forEach(rating -> {
            UpdateResultDto indexResult = ratingService.saveRating(trucks.get(0).getId(), rating);
            assertThat(indexResult.getResult(), is("UPDATED"));
            assertThat(indexResult.getId(), is(rating.getId()));
        });

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(trucks.get(0).getId());
        List<Rating> truck1Ratings = truck.getRatings();

        assertThat(truck.getNumRating(), is(ratings.size()));
        assertThat(truck1Ratings.size(), is(ratings.size()));

        double calculatedStarAvg = ratings.stream().mapToDouble(rating -> rating.getStar()).average().getAsDouble();
        assertThat((double) truck.getStarAvg(), closeTo(calculatedStarAvg, 0.0001f));
    }

    @Test
    void testUpdateRatingUpdateTruckStarAvg() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(trucks.get(0).getId(), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        float starToUpdate = ratings.get(0).getStar() + 1.0f;
        String commentToUpdate = "정말 재밌어요ㅋㅋ";
        ratings.get(0).setStar(starToUpdate);
        ratings.get(0).setComment(commentToUpdate);
        UpdateResultDto updateResult = ratingService.updateRating(trucks.get(0).getId(), ratings.get(0));

        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(trucks.get(0).getId());
        double calculatedStarAvg = ratings.stream().mapToDouble(rating -> rating.getStar()).average().getAsDouble();
        assertThat((double) truck.getStarAvg(), closeTo(calculatedStarAvg, 0.0001f));
    }

    @Test
    void testDeleteRating() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(trucks.get(0).getId(), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        UpdateResultDto deleteResult = ratingService.deleteRating(trucks.get(0).getId(), ratings.get(0).getId());
        assertThat(deleteResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(trucks.get(0).getId());
        List<Rating> truck1Ratings = truck.getRatings();

        assertThat(truck.getNumRating(), is(ratings.size() - 1));
        assertThat(truck1Ratings.size(), is(ratings.size() - 1));
        assertThat(truck1Ratings, hasItems(ratings.get(1), ratings.get(2)));
        assertThat(truck1Ratings, not(hasItem(ratings.get(0))));

        assertThat((double) truck.getStarAvg(), closeTo((ratings.get(1).getStar() + ratings.get(2).getStar()) / 2, 0.0001f));
    }

    @Test
    void testFindByUserIdResultIsInCreatedDateReverseOrder() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(trucks.get(0).getId(), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String commonUserId = ratings.get(0).getUserId();

        List<MyRatingDto> searchedRatings = ratingService.findByUserId(commonUserId).getDocs();
        assertThat(searchedRatings.stream().map(r -> r.getId()).collect(Collectors.toList()), 
                    contains(ratings.get(2).getId(), ratings.get(1).getId(), ratings.get(0).getId())
        );
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
                            .userId(UUID.randomUUID().toString())
                            .build();

        Truck truck2 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck2")
                            .geoLocation(GeoLocation.builder().lat(40).lon(140).build())
                            .description("this is truck2")
                            .opened(true)
                            .userId(UUID.randomUUID().toString())
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

        String userId = UUID.randomUUID().toString();

        Rating rating1 = Rating.builder()
                                .userId(userId)
                                .comment("hello1")
                                .star(3.0f)
                                .build();

        Rating rating2 = Rating.builder()
                                .userId(userId)
                                .comment("hello2")
                                .star(5.0f)
                                .build();

        Rating rating3 = Rating.builder()
                                .userId(userId)
                                .comment("hello3")
                                .star(1.0f)
                                .build();

        return Arrays.asList(rating1, rating2, rating3);
    }

    private void indexTestRatingData(String truckId, List<Rating> ratings) {

        ratings.forEach(rating-> {
            UpdateResultDto updateResult = ratingService.saveRating(truckId, rating);
            log.info("rating index result: " + updateResult.getResult());
            assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
