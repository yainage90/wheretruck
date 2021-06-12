package com.gamakdragons.wheretruck.domain.food.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gamakdragons.wheretruck.cloud.aws.service.S3ServiceImpl;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.ElasticSearchConfig;
import com.gamakdragons.wheretruck.config.S3Config;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.dto.FoodUpdateRequestDto;
import com.gamakdragons.wheretruck.domain.food.entity.Food;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
    classes = {TruckServiceImpl.class, FoodServiceImpl.class, ElasticSearchServiceImpl.class, S3ServiceImpl.class, ElasticSearchConfig.class, S3Config.class},
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class FoodServiceImplTest {

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;


    @Value("${cloud.aws.s3.bucket.food_image}")
    private String FOOD_IMAGE_BUCKET;

    @Autowired
    private FoodService foodService;

    @Autowired
    private TruckService truckService;

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
    public void beforeEach() throws IOException {
        initRestHighLevelClient();
        initS3Client();
        deleteTestTruckIndex();
        createTestTruckIndex();
        createS3Bucket(FOOD_IMAGE_BUCKET);
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteTestTruckIndex();
        deleteAllBucketObjects();
        deleteS3Bucket(FOOD_IMAGE_BUCKET);
    }

    @Test
    void testSaveFood() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();

        foodSaveRequestDtos.stream().forEach(foodSaveRequestDto -> {
            UpdateResultDto indexResult = foodService.saveFood(trucks.get(0).getId(), foodSaveRequestDto);
            assertThat(indexResult.getResult(), is("UPDATED"));
            assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, trucks.get(0).getId() + indexResult.getId()), is(true));
        });

        Truck truck = truckService.getById(trucks.get(0).getId());
        assertThat(truck.getFoods(), hasSize(2));
    }

    @Test
    void testUpdateFood() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);
        
        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();
        indexTestFoodData(trucks.get(0).getId(), foodSaveRequestDtos);

        List<Food> foods = truckService.getById(trucks.get(0).getId()).getFoods();
        foods.forEach(food -> {
            String nameToUpdate = "updated" + food.getName();
            int costToUpdate = food.getCost() * 10;
            String descriptionToUpdate = food.getDescription() + " updated";
            byte[] imageBinary = new byte[128];
            new Random().nextBytes(imageBinary);
            MockMultipartFile imageToUpdate = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

            FoodUpdateRequestDto foodUpdateRequestDto = new FoodUpdateRequestDto();
            foodUpdateRequestDto.setId(food.getId());
            foodUpdateRequestDto.setName(nameToUpdate);
            foodUpdateRequestDto.setCost(costToUpdate);
            foodUpdateRequestDto.setDescription(descriptionToUpdate);
            foodUpdateRequestDto.setImage(imageToUpdate);

            UpdateResultDto updateResult = foodService.updateFood(trucks.get(0).getId(), foodUpdateRequestDto);
            assertThat(updateResult.getResult(), is("UPDATED"));
            
            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            Food updatedFood = truckService.getById(trucks.get(0).getId()).getFoods().stream()
                                                .filter(f-> f.getId().equals(food.getId()))
                                                .findFirst().get();

            assertThat(updatedFood.getName(), is(nameToUpdate));
            assertThat(updatedFood.getCost(), is(costToUpdate));
            assertThat(updatedFood.getDescription(), is(descriptionToUpdate));
            assertThat(updatedFood.getImageUrl(), is(food.getImageUrl()));

        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDeleteFood() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();
        indexTestFoodData(trucks.get(0).getId(), foodSaveRequestDtos);


        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<Food> foods = truckService.getById(trucks.get(0).getId()).getFoods();
        foods.forEach(food -> {
            UpdateResultDto deleteResult = foodService.deleteFood(trucks.get(0).getId(), food.getId());
            assertThat(deleteResult.getResult(), is("UPDATED"));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(truckService.getById(trucks.get(0).getId()).getFoods(), hasSize(0));

    }

    @Test
    void testSortFoods() {

        List<Truck> trucks = createTestTruckData();
        indexTestTruckData(trucks);

        List<String> createdIds = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
            foodSaveRequestDto.setName("food" + i);
            foodSaveRequestDto.setCost(100);
            foodSaveRequestDto.setDescription("this is food" + i);
            foodSaveRequestDto.setImage(null);

            UpdateResultDto updateResultDto = foodService.saveFood(trucks.get(0).getId(), foodSaveRequestDto);
            createdIds.add(updateResultDto.getId());
        }

        log.info(createdIds.toString());

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Collections.shuffle(createdIds);
        log.info(createdIds.toString());

        foodService.sortFoods(trucks.get(0).getId(), createdIds);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<Food> foods = truckService.getById(trucks.get(0).getId()).getFoods();
        List<String> sortedIds = foods.stream().map(food -> food.getId()).collect(Collectors.toList());
        assertThat(sortedIds, equalTo(createdIds));
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
                            .geoLocation(new GeoLocation(30, 130))
                            .description("this is truck1")
                            .opened(false)
                            .userId("userid1")
                            .build();

        Truck truck2 = Truck.builder()
                            .id(UUID.randomUUID().toString())
                            .name("truck2")
                            .geoLocation(new GeoLocation(40, 140))
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

    private List<FoodSaveRequestDto> createTestFoodSaveRequestDto() {

        FoodSaveRequestDto foodSaveRequestDto1 = new FoodSaveRequestDto();
        foodSaveRequestDto1.setName("food1");
        foodSaveRequestDto1.setCost(10000);
        foodSaveRequestDto1.setDescription("this is food1");
        byte[] imageBinary1 = new byte[128];
        new Random().nextBytes(imageBinary1);
        MockMultipartFile image1 = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary1);
        foodSaveRequestDto1.setImage(image1);
        
        FoodSaveRequestDto foodSaveRequestDto2 = new FoodSaveRequestDto();
        foodSaveRequestDto2.setName("food2");
        foodSaveRequestDto2.setCost(10000);
        foodSaveRequestDto2.setDescription("this is food2");
        byte[] imageBinary2 = new byte[128];
        new Random().nextBytes(imageBinary2);
        MockMultipartFile image2 = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary2);
        foodSaveRequestDto2.setImage(image2);
        
        return Arrays.asList(foodSaveRequestDto1, foodSaveRequestDto2);
    }

    private void indexTestFoodData(String truckId, List<FoodSaveRequestDto> foodSaveRequestDtos) {

        foodSaveRequestDtos.forEach(foodSaveRequestDto -> {
            UpdateResultDto updateResult = foodService.saveFood(truckId, foodSaveRequestDto);
            log.info("food index result: " + updateResult.getResult() + ", food id: " + foodSaveRequestDto.getId());
            assertThat(updateResult.getResult(), is("UPDATED"));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void initS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();
    }

    public void createS3Bucket(String bucketName) {

        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(new CreateBucketRequest(bucketName));
            if(s3Client.doesBucketExistV2(bucketName)) {
                log.info("bucket created: " + bucketName);
            } else {
                log.error("bucket create failed: " + bucketName);
            }
        }

        log.info("bucket exists: " + bucketName);
    }

    public void deleteS3Bucket(String bucketName) {
        if(s3Client.doesBucketExistV2(bucketName)) {
            s3Client.deleteBucket(new DeleteBucketRequest(bucketName));
            if(!s3Client.doesBucketExistV2(bucketName)) {
                log.info("bucket deleted: " + bucketName);
            } else {
                log.error("bucket exists but delete bucket failed: " + bucketName);
            }
        }

        log.info("bucket does not exist: " + bucketName);
    }

    private void deleteAllBucketObjects() {

        try {
            ObjectListing objectListing = s3Client.listObjects(FOOD_IMAGE_BUCKET);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    s3Client.deleteObject(FOOD_IMAGE_BUCKET, objIter.next().getKey());
                }

                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }

            s3Client.deleteBucket(FOOD_IMAGE_BUCKET);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

}
