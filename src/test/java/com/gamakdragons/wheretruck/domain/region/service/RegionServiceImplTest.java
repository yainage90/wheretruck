package com.gamakdragons.wheretruck.domain.region.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.gamakdragons.wheretruck.TestIndexUtil;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.domain.region.entity.Region;
import com.gamakdragons.wheretruck.util.EsRequestFactory;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = {RegionServiceImpl.class, ElasticSearchServiceImpl.class, ElasticSearchConfig.class, TestIndexUtil.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class RegionServiceImplTest {

    @Autowired
    private RegionService service;
    
    @Value("${elasticsearch.index.region.name}")
    private String TEST_REGION_INDEX_NAME;

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
        TestIndexUtil.deleteTestRegionIndex();
        TestIndexUtil.createTestRegionIndex();
    }

    @AfterEach
    public void afterEach() throws IOException {
        TestIndexUtil.deleteTestRegionIndex();
    }

    @Test
    void testFindAll() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        SearchResultDto<Region> result = service.findAll();

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(regions.size()));
        assertThat(result.getDocs(), contains(regions));
    }

    @Test
    void testFindByAddress() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        SearchResultDto<Region> result = service.findByAddress(regions.get(0).getCity(), regions.get(0).getTown());
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1));
        assertThat(result.getDocs(), hasItem(regions.get(0)));

    }

    @Test
    void testFindByAddressFailsIfNoSuchAddress() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        SearchResultDto<Region> result = service.findByAddress(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(0));
    }

    @Test
    void testFindByLocationDocsAreInShorterDistanceOrder() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        float region2Lat = regions.get(1).getGeoLocation().getLat() + 0.2f;
        float region2Lon = regions.get(1).getGeoLocation().getLon() + 0.2f;

        SearchResultDto<Region> result = service.findByLocation(
                new GeoLocation(region2Lat, region2Lon), 500
        );

        result.getDocs().forEach(region -> {
            log.info(region.getRegionName());
        });

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(3));
        assertThat(result.getDocs(), contains(regions.get(1), regions.get(2), regions.get(0)));
    }

    @Test
    void testFindByLocationDocsOnlyContainsInDistance() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        float region2Lat = regions.get(1).getGeoLocation().getLat() + 0.000001f;
        float region2Lon = regions.get(1).getGeoLocation().getLon() + 0.000001f;

        SearchResultDto<Region> result = service.findByLocation(
            new GeoLocation(region2Lat, region2Lon), 10
        );

        result.getDocs().forEach(region -> {
            log.info(region.getRegionName());
        });

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1));
        assertThat(result.getDocs(), hasItem(regions.get(1)));
    }

    @Test
    void testFindByLocationDocsEmptyIfAnyRegionInDistance() {

        List<Region> regions = createTestRegionData();
        indexTestData(regions);

        float pivotLat = regions.get(0).getGeoLocation().getLat() - 10.0f;
        float pivotLon = regions.get(0).getGeoLocation().getLon() - 10.0f;

        SearchResultDto<Region> result = service.findByLocation(new GeoLocation(pivotLat, pivotLon), 10.0f);
        log.info(result.toString());

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(0));
        assertThat(result.getDocs(), hasSize(0));
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

    

    private List<Region> createTestRegionData() {

        Region region1 = Region.builder()
                            .regionName("region1")
                            .city("서울특별시").town("관악구")
                            .geoLocation(new GeoLocation(30.0f, 130.0f))
                            .build();
        Region region2 = Region.builder()
                            .regionName("region2")
                            .geoLocation(new GeoLocation(31.0f, 131.0f))
                            .build();
        
        Region region3 = Region.builder()
                            .regionName("region3")
                            .geoLocation(new GeoLocation(32.0f, 132.0f))
                            .build();

        return Arrays.asList(region1, region2, region3);
        
    }

    private void indexTestData(List<Region> regions) {

        regions.forEach(region -> {
            IndexRequest request = EsRequestFactory.createIndexRequest(TEST_REGION_INDEX_NAME, UUID.randomUUID().toString(), region);
            try {
                IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
                log.info(response.getResult().name());
            } catch(IOException e) {
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
