package com.gamakdragons.wheretruck.truck.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import static org.hamcrest.number.IsCloseTo.closeTo;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.truck.model.Truck;

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

@SpringBootTest(classes = {TruckServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class TruckServiceTest {

    @Autowired
    private TruckService service;
    
    @Value("${es.index.truck.name}")
    private String TEST_TRUCK_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private Truck truck1;
    private Truck truck2;

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
    void testFindAll() {
        
        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getId(), is(truck1.getId()));
        assertThat(indexResult2.getId(), is(truck2.getId()));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Truck> result = service.findAll();
        log.info(result.toString());

        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(2L));

        assertThat(result.getDocs(), hasItems(truck1, truck2));
    }

    @Test
    void testFindByLocation() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Truck> result = service.findByLocation(GeoLocation.builder().lat(30.1f).lon(130.1f).build(), 100.0f);
        log.info(result.toString());

        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));

        assertThat(result.getDocs(), hasItem(truck1));
        assertThat(result.getDocs(), not(hasItem(truck2)));

    }

    @Test
    void testFindByUserId() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Truck> result = service.findByUserId("1");
        log.info(result.toString());
        
        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));

        assertThat(result.getDocs().get(0), equalTo(truck1));
    }

    @Test
    void testGetById() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck result1 = service.getById(truck1.getId());
        log.info(result1.toString());
        assertThat(result1, equalTo(truck1));

        Truck result2 = service.getById(truck2.getId());
        log.info(result2.toString());
        assertThat(result2, equalTo(truck2));

    }

    @Test
    void testSaveTruck() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult1.getId(), is(truck1.getId()));

        assertThat(indexResult2.getResult(), is("CREATED"));
        assertThat(indexResult2.getId(), is(truck2.getId()));

    }

    @Test
    void testUpdateTruck() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        String descriptionToUpdate = "this is updated truck2";
        truck1.setDescription(descriptionToUpdate);
        UpdateResultDto updateResult = service.updateTruck(truck1);
        assertThat(updateResult.getResult(), equalTo("UPDATED"));
        Truck updatedTruck1 = service.getById(truck1.getId());
        assertThat(updatedTruck1.getDescription(), equalTo(descriptionToUpdate));

    }

    @Test
    void testDeleteTruck() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        DeleteResultDto deleteResult1 = service.deleteTruck(truck1.getId());
        assertThat(deleteResult1.getResult(), is("DELETED"));

        DeleteResultDto deleteResult2 = service.deleteTruck(truck2.getId());
        assertThat(deleteResult2.getResult(), is("DELETED"));

        assertThat(service.getById(truck1.getId()), nullValue());
        assertThat(service.getById(truck2.getId()), nullValue());

    }

    @Test
    void testOpenTruck() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        UpdateResultDto updateResult1 = service.openTruck(truck1.getId(), GeoLocation.builder().lat(33.0f).lon(133.0f).build());
        assertThat(updateResult1.getResult(), is("UPDATED"));

        Truck startedTruck1 = service.getById(truck1.getId());
        assertThat(startedTruck1.isOpened(), is(true));
        assertThat((double) startedTruck1.getGeoLocation().getLat(), closeTo(33.0, 0.001));
    }

    @Test
    void testStopTruck() {

        IndexResultDto indexResult1 = service.saveTruck(truck1);
        log.info("truck1 index result: " + indexResult1.getResult() + ", truck1 id: " + indexResult1.getId());
        IndexResultDto indexResult2 = service.saveTruck(truck2);
        log.info("truck2 index result: " + indexResult2.getResult() + ", truck2 id: " + indexResult2.getId());

        assertThat(indexResult1.getResult(), is("CREATED"));
        assertThat(indexResult2.getResult(), is("CREATED"));

        UpdateResultDto updateResult1 = service.stopTruck(truck2.getId());
        assertThat(updateResult1.getResult(), is("UPDATED"));

        Truck startedTruck1 = service.getById(truck1.getId());
        assertThat(startedTruck1.isOpened(), is(false));
    }

    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

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

        truck1 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck1")
                            .geoLocation(GeoLocation.builder().lat(30).lon(130).build())
                            .description("this is truck1")
                            .opened(false)
                            .userId("1")
                            .build();

        truck2 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck2")
                            .geoLocation(GeoLocation.builder().lat(40).lon(140).build())
                            .description("this is truck2")
                            .opened(true)
                            .userId("2")
                            .build();
    }

    private void deleteTestIndex() throws IOException {
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
