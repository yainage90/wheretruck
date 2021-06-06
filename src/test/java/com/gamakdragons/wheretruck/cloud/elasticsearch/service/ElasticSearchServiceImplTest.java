package com.gamakdragons.wheretruck.cloud.elasticsearch.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.cloud.elasticsearch.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.util.EsRequestFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
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
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
    classes = {ElasticSearchServiceImpl.class, ElasticSearchConfig.class}, 
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class ElasticSearchServiceImplTest {

    @Value("${elasticsearch.host}")
    private String ES_HOST;

    @Value("${elasticsearch.port}")
    private int ES_PORT;

    @Value("${elasticsearch.username}")
    private String ES_USER;

    @Value("${elasticsearch.password}")
    private String ES_PASSWORD;

    @Autowired
    private ElasticSearchService service;

    private RestHighLevelClient esClient;

    private String testIndex;

    @BeforeEach
    public void beforeEach() throws IOException {
        testIndex = "test";
        initRestHighLevelClient();
        createTestIndex();

    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestkIndex();
    }

    @Test
    void testIndex() throws IOException {
        String id = UUID.randomUUID().toString();
        String name = "test";
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        IndexRequest indexRequest = EsRequestFactory.createIndexRequest(testIndex, id, obj);

        IndexResponse response = service.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(response.getResult(), is(Result.CREATED));
        assertThat(response.getId(), is(id));
    }

    @Test
    void testUpdate() throws IOException {

        String id = UUID.randomUUID().toString();
        String name = "test";
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        IndexRequest indexRequest = EsRequestFactory.createIndexRequest(testIndex, id, obj);

        IndexResponse indexResponse = service.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(indexResponse.getId(), is(id));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String updatedName = "updatedName";
        JsonObject updatedObj = new JsonObject();
        updatedObj.addProperty("id", id);
        updatedObj.addProperty("name", updatedName);

        UpdateRequest updateRequest = EsRequestFactory.createUpdateRequest(testIndex, id, updatedObj);
        UpdateResponse updateResponse = service.update(updateRequest, RequestOptions.DEFAULT);

        assertThat(updateResponse.getId(), is(id));
        assertThat(updateResponse.getResult(), is(Result.UPDATED));
        assertThat(updateResponse.getVersion(), is(2L));
    }

    @Test
    void testDelete() throws IOException {
        
        String id = UUID.randomUUID().toString();
        String name = "test";
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        IndexRequest indexRequest = EsRequestFactory.createIndexRequest(testIndex, id, obj);

        IndexResponse indexResponse = service.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(indexResponse.getId(), is(id));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteRequest deleteRequest = EsRequestFactory.createDeleteByIdRequest(testIndex, id);
        DeleteResponse deleteResponse = service.delete(deleteRequest, RequestOptions.DEFAULT);
        assertThat(deleteResponse.getId(), is(id));
        assertThat(deleteResponse.getResult(), is(Result.DELETED));
    }

    @Test
    void testGet() throws IOException, JSONException {

        String id = UUID.randomUUID().toString();
        String name = "test";
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        IndexRequest indexRequest = EsRequestFactory.createIndexRequest(testIndex, id, obj);

        IndexResponse indexResponse = service.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(indexResponse.getId(), is(id));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        GetRequest getRequest = EsRequestFactory.createGetRequest(testIndex, id);
        GetResponse getResponse = service.get(getRequest, RequestOptions.DEFAULT);

        assertThat(new Gson().fromJson(getResponse.getSourceAsString(), JsonObject.class), equalTo(obj));
    }
    
    @Test
    void testSearch() throws IOException {

        String id = UUID.randomUUID().toString();
        String name = "test";
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        IndexRequest indexRequest = EsRequestFactory.createIndexRequest(testIndex, id, obj);

        IndexResponse indexResponse = service.index(indexRequest, RequestOptions.DEFAULT);
        assertThat(indexResponse.getId(), is(id));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchRequest searchRequest = EsRequestFactory.createSearchAllRequest(testIndex);
        SearchResponse searchResponse = service.search(searchRequest, RequestOptions.DEFAULT);

        assertThat(searchResponse.getHits().getTotalHits().value, is(1L));
    }

    public void initRestHighLevelClient() {

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

    private void createTestIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(testIndex);

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

    private void deleteTestkIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(testIndex);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(testIndex);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }
}

