package com.gamakdragons.wheretruck.cloud.elasticsearch.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.config.ElasticSearchConfig;
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
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
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
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
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

    @Test
    void testDeleteByQuery() throws IOException {

        String id1 = UUID.randomUUID().toString();
        String name1 = "test1";

        JsonObject obj1 = new JsonObject();
        obj1.addProperty("id", id1);
        obj1.addProperty("name", name1);
        IndexRequest indexRequest1 = EsRequestFactory.createIndexRequest(testIndex, id1, obj1);

        String id2 = UUID.randomUUID().toString();
        String name2 = "test2";

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("id", id2);
        obj2.addProperty("name", name2);
        IndexRequest indexRequest2 = EsRequestFactory.createIndexRequest(testIndex, id2, obj2);

        IndexResponse indexResponse1 = service.index(indexRequest1, RequestOptions.DEFAULT);
        assertThat(indexResponse1.getId(), is(id1));
        IndexResponse indexResponse2 = service.index(indexRequest2, RequestOptions.DEFAULT);
        assertThat(indexResponse2.getId(), is(id2));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        DeleteByQueryRequest request = EsRequestFactory.createDeleteByQuerydRequest(new String[]{testIndex}, "name", name2);
        BulkByScrollResponse response = service.deleteByQuery(request, RequestOptions.DEFAULT);
        assertThat(response.getDeleted(), is(1L));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        GetRequest getObj1Request = EsRequestFactory.createGetRequest(testIndex, obj1.get("id").getAsString());
        GetResponse getObj1Response = service.get(getObj1Request, RequestOptions.DEFAULT);
        GetRequest getObj2Request = EsRequestFactory.createGetRequest(testIndex, obj2.get("id").getAsString());
        GetResponse getObj2Response = service.get(getObj2Request, RequestOptions.DEFAULT);

        assertThat(getObj1Response.getSource(), is(not(nullValue())));
        assertThat(getObj2Response.getSource(), is(nullValue()));
    }

    @Test
    void testMultiGet() throws IOException {

        String id1 = UUID.randomUUID().toString();
        String name1 = "test1";

        JsonObject obj1 = new JsonObject();
        obj1.addProperty("id", id1);
        obj1.addProperty("name", name1);
        IndexRequest indexRequest1 = EsRequestFactory.createIndexRequest(testIndex, id1, obj1);

        String id2 = UUID.randomUUID().toString();
        String name2 = "test2";

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("id", id2);
        obj2.addProperty("name", name2);
        IndexRequest indexRequest2 = EsRequestFactory.createIndexRequest(testIndex, id2, obj2);

        IndexResponse indexResponse1 = service.index(indexRequest1, RequestOptions.DEFAULT);
        assertThat(indexResponse1.getId(), is(id1));
        IndexResponse indexResponse2 = service.index(indexRequest2, RequestOptions.DEFAULT);
        assertThat(indexResponse2.getId(), is(id2));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String[] includes = new String[]{"id"};
        String[] excludes = new String[]{"name"};
        MultiGetRequest request = EsRequestFactory.createMultiGetRequest(testIndex, Arrays.asList(id1, id2), includes, excludes);

        MultiGetResponse response = service.multiGet(request, RequestOptions.DEFAULT);
        assertThat(response.getResponses().length, is(2));

        Arrays.stream(response.getResponses()).forEach(item -> {
            assertThat(item.getResponse().getSourceAsMap().containsKey("id"), is(true));
            assertThat(item.getResponse().getSourceAsMap().containsKey("name"), is(false));
        });

        List<String> ids = Arrays.stream(response.getResponses()).map(item -> item.getResponse().getSource().get("id").toString()).collect(Collectors.toList());
        assertThat(ids, hasItems(id1, id2));
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

