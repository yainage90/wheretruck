package com.gamakdragons.wheretruck;

import java.io.IOException;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TestIndexUtil {

    private static String TEST_TRUCK_INDEX;
    private static String TEST_REGION_INDEX;
    private static String TEST_FAVORITE_INDEX;
    private static String TEST_USER_INDEX;

    private static RestHighLevelClient esClient;

	@Value("${elasticsearch.index.region.name}")
	public void injectRegionIndexName(String value) {
		TEST_REGION_INDEX = value;
	}

	@Value("${elasticsearch.index.truck.name}")
	public void injectTruckIndexName(String value) {
		TEST_TRUCK_INDEX = value;
	}

	@Value("${elasticsearch.index.favorite.name}")
	public void injectFavoriteIndexName(String value) {
		TEST_FAVORITE_INDEX = value;
	}

	@Value("${elasticsearch.index.user.name}")
	public void injectUserIndexname(String value) {
		TEST_USER_INDEX = value;
	}

    private static ElasticsearchContainer elasticsearchContainer;

    public static void createElasticSearchTestContainer() {

        DockerImageName dockerImage = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch").withTag("7.7.0");
        elasticsearchContainer = new ElasticsearchContainer(dockerImage)
                                        .withPassword("test");
        elasticsearchContainer.start();

        System.setProperty("elasticsearch.address", elasticsearchContainer.getHttpHostAddress());
    }

    public static void closeElasticSearchTestContainer() {
        elasticsearchContainer.close();
        elasticsearchContainer.stop();
    }

	public static void initRestHighLevelClient() {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "test"));

        RestClientBuilder builder = RestClient.builder(
            HttpHost.create(elasticsearchContainer.getHttpHostAddress())
        )
        .setHttpClientConfigCallback((httpClientBuilder) -> {
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });

        esClient = new RestHighLevelClient(builder);
    }

	public static void createTestIndices() throws IOException {
		createTestRegionIndex();
		createTestTruckIndex();
		createTestFavoriteIndex();
		createTestUserIndex();
	}

	public static void deleteTestIndices() throws IOException {
		deleteTestRegionIndex();
		deleteTestTruckIndex();
		deleteTestFavoriteIndex();
		deleteTestUserIndex();
	}

	public static void createTestRegionIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_REGION_INDEX);

        request.settings(Settings.builder()
            .put("index.number_of_shards", 1)
            .put("index.number_of_replicas", 0)
        );

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("regionName");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("regionType");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();

                builder.startObject("city");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("roadAddress");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("postAddress");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("geoLocation");
                {
                    builder.field("type", "geo_point");
                }
                builder.endObject();
                
                builder.startObject("capacity");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();

                builder.startObject("cost");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("permissionStartDate");
                {
                    builder.field("type", "date");
                }
                builder.endObject();

                builder.startObject("permissionEndDate");
                {
                    builder.field("type", "date");
                }
                builder.endObject();

                builder.startObject("closedDays");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("weekdayStartTime");
                {
                    builder.field("type", "date");
                    builder.field("format", "hour_minute");
                }
                builder.endObject();

                builder.startObject("weekdayEndTime");
                {
                    builder.field("type", "date");
                    builder.field("format", "hour_minute");
                }
                builder.endObject();

                builder.startObject("weekendStartTime");
                {
                    builder.field("type", "date");
                    builder.field("format", "hour_minute");
                }
                builder.endObject();

                builder.startObject("weekendEndTime");
                {
                    builder.field("type", "date");
                    builder.field("format", "hour_minute");
                }
                builder.endObject();

                builder.startObject("restrictedItems");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("agencyName");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("agencyTel");
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

	public static void deleteTestRegionIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_REGION_INDEX);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_REGION_INDEX);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

	public static void createTestTruckIndex() throws IOException {

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

                builder.startObject("imageUrl");
                {
                    builder.field("type", "keyword");
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

	public static void deleteTestTruckIndex() throws IOException {
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
	

	public static void createTestFavoriteIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_FAVORITE_INDEX);

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

	public static void deleteTestFavoriteIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_FAVORITE_INDEX);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_FAVORITE_INDEX);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

	public static void createTestUserIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_USER_INDEX);

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

                builder.startObject("nickName");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                
                builder.startObject("role");
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

	public static void deleteTestUserIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_USER_INDEX);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_USER_INDEX);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

    
}
