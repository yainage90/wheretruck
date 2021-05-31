package com.gamakdragons.wheretruck.truck.service;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
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

    @BeforeEach
    public void beforeEach() throws IOException {
        createRestHighLevelClient();
        createTestIndex();
    }

    
    @Test
    void testFindAll() {

    }

    @Test
    void testFindByLocation() {

    }

    @Test
    void testFindByUserId() {

    }

    @Test
    void testGetById() {

    }

    @Test
    void testSaveTruck() {

    }

    @Test
    void testUpdateTruck() {

    }

    @Test
    void testDeleteTruck() {

    }

    @Test
    void testOpenTruck() {

    }

    @Test
    void testStopTruck() {

    }

    private void createRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestIndex() throws IOException {

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

        request.mapping("_source", builder);

        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        if(!response.isShardsAcknowledged()) {
            throw new IOException();
        }
    }

    private void indexTestData() {
        
    }
    

}
