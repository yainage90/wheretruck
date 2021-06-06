package com.gamakdragons.wheretruck.domain.truck.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.cloud.elasticsearch.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;

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

@SpringBootTest(
    classes = {TruckServiceImpl.class, ElasticSearchServiceImpl.class, ElasticSearchConfig.class}, 
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class TruckServiceTest {

    @Autowired
    private TruckService service;
    
    @Value("${elasticsearch.index.truck.name}")
    private String TEST_TRUCK_INDEX_NAME;

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
        deleteTestTruckIndex();
        createTestTruckIndex();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestTruckIndex();
    }

    @Test
    void testFindAll() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        SearchResultDto<Truck> result = service.findAll();
        log.info(result.toString());

        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(2));

        List<Truck> expectedTrucks = testTrucks.stream()
            .map(truck -> Truck.builder()
                            .id(truck.getId())
                            .name(truck.getName())
                            .geoLocation(truck.getGeoLocation())
                            .description(truck.getDescription())
                            .opened(truck.isOpened())
                            .userId(truck.getUserId())
                            .numRating(truck.getNumRating())
                            .starAvg(truck.getStarAvg())
                            .foods(null)
                            .ratings(null)
                            .build())
            .collect(Collectors.toList());

        expectedTrucks.forEach(expectedTruck -> {
            assertThat(result.getDocs(), hasItem(expectedTruck));
        });
    }

    @Test
    void testFindByLocation() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        SearchResultDto<Truck> result = service.findByGeoLocation(
            GeoLocation.builder()
                .lat(testTrucks.get(0).getGeoLocation().getLat() + 0.1f)
                .lon(testTrucks.get(0).getGeoLocation().getLon() + 0.1f)
                .build(), 
            100
        );

        log.info(result.toString());
        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1));

        List<Truck> expectedTrucks = testTrucks.stream()
            .filter(truck -> truck.getId().equals(testTrucks.get(0).getId()))
            .map(truck -> Truck.builder()
            .id(truck.getId())
            .name(truck.getName())
            .geoLocation(truck.getGeoLocation())
            .description(truck.getDescription())
            .opened(truck.isOpened())
            .userId(truck.getUserId())
            .numRating(truck.getNumRating())
            .starAvg(truck.getStarAvg())
            .foods(null)
            .ratings(null)
            .build())
            .collect(Collectors.toList());

        expectedTrucks.forEach(expectedTruck -> {
            assertThat(result.getDocs(), hasItem(expectedTruck));
        });
    }

    @Test
    void testFindByUserId() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        SearchResultDto<Truck> result = service.findByUserId(testTrucks.get(0).getUserId());
        log.info(result.toString());
        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1));

        List<Truck> expectedTrucks = testTrucks.stream()
            .filter(truck -> truck.getUserId().equals(testTrucks.get(0).getUserId()))
            .map(truck -> Truck.builder()
            .id(truck.getId())
            .name(truck.getName())
            .geoLocation(truck.getGeoLocation())
            .description(truck.getDescription())
            .opened(truck.isOpened())
            .userId(truck.getUserId())
            .numRating(truck.getNumRating())
            .starAvg(truck.getStarAvg())
            .foods(Collections.emptyList())
            .ratings(Collections.emptyList())
            .build())
            .collect(Collectors.toList());
        
        expectedTrucks.forEach(expectedTruck -> {
            assertThat(result.getDocs(), hasItem(expectedTruck));
        });
    }

    @Test
    void testGetById() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        Truck result = service.getById(testTrucks.get(0).getId());
        log.info(result.toString());

        Truck expectedTruck = Truck.builder()
                                    .id(testTrucks.get(0).getId())
                                    .name(testTrucks.get(0).getName())
                                    .geoLocation(testTrucks.get(0).getGeoLocation())
                                    .description(testTrucks.get(0).getDescription())
                                    .opened(testTrucks.get(0).isOpened())
                                    .userId(testTrucks.get(0).getUserId())
                                    .numRating(testTrucks.get(0).getNumRating())
                                    .starAvg(testTrucks.get(0).getStarAvg())
                                    .foods(Collections.emptyList())
                                    .ratings(Collections.emptyList())
                                    .build();

        assertThat(result, equalTo(expectedTruck));
    }

    @Test
    void testSaveTruck() {

        List<Truck> testTrucks = createTestTruckData();

        testTrucks.stream().forEach(truck -> {
            IndexResultDto indexResult = service.saveTruck(truck);
            log.info("truck index result: " + indexResult.getResult() + ", truck id: " + indexResult.getId());

            assertThat(indexResult.getResult(), is("CREATED"));
            assertThat(indexResult.getId(), is(truck.getId()));
        });
    }

    @Test
    void testUpdateTruck() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        String nameToUpdate = "updatedTruck1";
        GeoLocation geoLocationToUpdate = GeoLocation.builder().lat(30.0f  + (float) Math.random()).lon(130.0f + (float) Math.random()).build();
        String descriptionToUpdate = "this is updated truck1";

        testTrucks.get(0).setName(nameToUpdate);
        testTrucks.get(0).setGeoLocation(geoLocationToUpdate);
        testTrucks.get(0).setDescription(descriptionToUpdate);
        UpdateResultDto updateResult = service.updateTruck(testTrucks.get(0));
        assertThat(updateResult.getResult(), equalTo("UPDATED"));

        Truck updatedTruck = service.getById(testTrucks.get(0).getId());
        assertThat(updatedTruck.getName(), equalTo(nameToUpdate));
        assertThat(updatedTruck.getGeoLocation(), equalTo(geoLocationToUpdate));
        assertThat(updatedTruck.getDescription(), equalTo(descriptionToUpdate));

    }

    @Test
    void testDeleteTruck() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        DeleteResultDto deleteResult1 = service.deleteTruck(testTrucks.get(0).getId());
        assertThat(deleteResult1.getResult(), is("DELETED"));

        DeleteResultDto deleteResult2 = service.deleteTruck(testTrucks.get(1).getId());
        assertThat(deleteResult2.getResult(), is("DELETED"));

        assertThat(service.getById(testTrucks.get(0).getId()), nullValue());
        assertThat(service.getById(testTrucks.get(0).getId()), nullValue());
    }

    @Test
    void testOpenTruck() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);
        
        UpdateResultDto updateResult = service.openTruck(testTrucks.get(0).getId(), GeoLocation.builder().lat(33.0f).lon(133.0f).build());
        assertThat(updateResult.getResult(), is("UPDATED"));

        Truck startedTruck = service.getById(testTrucks.get(0).getId());
        assertThat(startedTruck.isOpened(), is(true));
        assertThat((double) startedTruck.getGeoLocation().getLat(), closeTo(33.0f, 0.001f));
        assertThat((double) startedTruck.getGeoLocation().getLon(), closeTo(133.0f, 0.001f));
    }

    @Test
    void testStopTruck() {

        List<Truck> testTrucks = createTestTruckData();
        indexTestTruckData(testTrucks);

        UpdateResultDto updateResult = service.stopTruck(testTrucks.get(1).getId());
        assertThat(updateResult.getResult(), is("UPDATED"));

        Truck stoppedTruck = service.getById(testTrucks.get(1).getId());
        assertThat(stoppedTruck.isOpened(), is(false));
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
                        builder.startObject("truckId");
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
                builder.startObject("ratings");
                {
                    builder.field("type", "nested");
                    builder.startObject("properties");
                        builder.startObject("id");
                        {
                            builder.field("type", "keyword");
                        }
                        builder.endObject();
                        builder.startObject("truckId");
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
            Thread.sleep(1500);
        } catch(InterruptedException e) {
            e.printStackTrace();
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
            IndexResultDto indexResult = service.saveTruck(truck);
            log.info("truck index result: " + indexResult.getResult() + ", truck id: " + indexResult.getId());
            assertThat(indexResult.getId(), is(truck.getId()));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
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
