package com.gamakdragons.wheretruck.region.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.io.IOException;
import java.util.UUID;

import com.gamakdragons.wheretruck.client.ElasticSearchRestClient;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.region.entity.Region;
import com.gamakdragons.wheretruck.util.EsRequestFactory;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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

@SpringBootTest(classes = {RegionServiceImpl.class, ElasticSearchRestClient.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class RegionServiceImplTest {

    @Autowired
    private RegionService service;
    
    @Value("${es.index.region.name}")
    private String TEST_REGION_INDEX_NAME;

    @Value("${es.host}")
    private String ES_HOST;

    @Value("${es.port}")
    private int ES_PORT;

    private RestHighLevelClient esClient;

    private Region region;

    @BeforeEach
    public void beforeEach() throws IOException {

        initRestHighLevelClient();
        deleteTestRegionIndex();
        createTestRegionIndex();
        indexTestData();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestRegionIndex();
    }

    @Test
    void testFindAll() {

        SearchResultDto<Region> result = service.findAll();

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));
        assertThat(result.getDocs(), hasItem(region));
    }

    @Test
    void testFindByAddress() {

        SearchResultDto<Region> result = service.findByAddress("서울특별시", "관악구");
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));
        assertThat(result.getDocs(), hasItem(region));

    }

    @Test
    void testFindByAddressFailsIfNoSuchAddress() {

        SearchResultDto<Region> result = service.findByAddress("서울특별시", "강남구");
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(0L));
    }

    @Test
    void testFindByLocation() {

        SearchResultDto<Region> result = service.findByLocation(GeoLocation.builder().lat(30.1f).lon(130.1f).build(), 100.0f);
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1L));
        assertThat(result.getDocs(), hasItem(region));
    }

    @Test
    void testFindByLocationFailsIfNoSuchLocation() {

        SearchResultDto<Region> result = service.findByLocation(GeoLocation.builder().lat(33.1f).lon(135.1f).build(), 100.0f);
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(0L));
    }


    private void initRestHighLevelClient() {
        RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, "http")
        );

        this.esClient = new RestHighLevelClient(builder);
    }

    private void createTestRegionIndex() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest(TEST_REGION_INDEX_NAME);

        request.settings(Settings.builder()
            .put("index.number_of_shards", 3)
            .put("index.number_of_replicas", 1)
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

    private void indexTestData() throws IOException {

        region = Region.builder()
                            .regionName("낙성대역")
                            .regionType(03)
                            .city("서울특별시")
                            .town("관악구")
                            .roadAddress("서울특별시 관악구 남부순환로 123-456")
                            .postAddress("서울특별시 관악구 낙성대동 81-12")
                            .geoLocation(GeoLocation.builder().lat(30.0f).lon(130.0f).build())
                            .capacity(5)
                            .cost("월 10,000원")
                            .permissionStartDate("2021-01-12")
                            .permissionEndDate("2021-12-03")
                            .closedDays("연중무휴")
                            .weekdayStartTime("10:00")
                            .weekdayEndTime("18:00")
                            .weekendStartTime("09:00")
                            .weekendEndTime("20:00")
                            .restrictedItems(null)
                            .agencyName("관악구청")
                            .agencyTel("02-123-4001")
                            .build();
        
        IndexRequest request = EsRequestFactory.createIndexRequest(TEST_REGION_INDEX_NAME, UUID.randomUUID().toString(), region);
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        log.info(response.getResult().name());

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void deleteTestRegionIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_REGION_INDEX_NAME);
        if(esClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
            DeleteIndexRequest request = new DeleteIndexRequest(TEST_REGION_INDEX_NAME);
            AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
            log.info("index deleted: " + response.isAcknowledged());
            if(!response.isAcknowledged()) {
                throw new IOException();
            }
        }

    }

}
