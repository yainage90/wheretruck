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
import java.util.stream.Collectors;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gamakdragons.wheretruck.TestIndexUtil;
import com.gamakdragons.wheretruck.cloud.aws.service.S3ServiceImpl;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.S3Config;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.entity.Food;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
import com.gamakdragons.wheretruck.domain.truck.service.TruckService;
import com.gamakdragons.wheretruck.domain.truck.service.TruckServiceImpl;
import com.gamakdragons.wheretruck.test_config.ElasticSearchTestConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(
    classes = {TruckServiceImpl.class, FoodServiceImpl.class, S3ServiceImpl.class, ElasticSearchTestConfig.class, S3Config.class, TestIndexUtil.class},
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class FoodServiceImplPlatformTest {

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
    
    @BeforeAll
    public static void beforeAll() {
        TestIndexUtil.createElasticSearchTestContainer();
    }

    @AfterAll
    public static void afterAll() {
        TestIndexUtil.closeElasticSearchTestContainer();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        TestIndexUtil.initRestHighLevelClient();
        TestIndexUtil.deleteTestTruckIndex();
        TestIndexUtil.createTestTruckIndex();

        initS3Client();
    }

    @AfterEach
    public void afterEach() throws IOException {
        TestIndexUtil.deleteTestTruckIndex();
        deleteAllBucketObjects();
        deleteS3Bucket(FOOD_IMAGE_BUCKET);
    }

    @Test
    void testSaveFood() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();

        foodSaveRequestDtos.stream().forEach(foodSaveRequestDto -> {
            UpdateResultDto indexResult = foodService.saveFood(truckIds.get(0), foodSaveRequestDto);
            assertThat(indexResult.getResult(), is("UPDATED"));

            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckIds.get(0) + "/" + indexResult.getId()), is(true));
        });

        Truck truck = truckService.getById(truckIds.get(0));
        assertThat(truck.getFoods(), hasSize(2));
    }

    @Test
    void testUpdateFood() {

        List<TruckSaveRequestDto> dtos = createTestTruckData();
        List<String> truckIds = indexTestTruckData(dtos);
        
        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();
        indexTestFoodData(truckIds.get(0), foodSaveRequestDtos);

        List<Food> foods = truckService.getById(truckIds.get(0)).getFoods();
        foods.forEach(food -> {
            String nameToUpdate = "updated" + food.getName();
            int costToUpdate = food.getCost() * 10;
            String descriptionToUpdate = food.getDescription() + " updated";
            byte[] imageBinary = new byte[128];
            new Random().nextBytes(imageBinary);
            MockMultipartFile imageToUpdate = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);

            FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
            foodSaveRequestDto.setId(food.getId());
            foodSaveRequestDto.setName(nameToUpdate);
            foodSaveRequestDto.setCost(costToUpdate);
            foodSaveRequestDto.setDescription(descriptionToUpdate);
            foodSaveRequestDto.setImage(imageToUpdate);

            UpdateResultDto updateResult = foodService.saveFood(truckIds.get(0), foodSaveRequestDto);
            assertThat(updateResult.getResult(), is("UPDATED"));
            
            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }

            Food updatedFood = truckService.getById(truckIds.get(0)).getFoods().stream()
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

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<FoodSaveRequestDto> foodSaveRequestDtos = createTestFoodSaveRequestDto();
        indexTestFoodData(truckIds.get(0), foodSaveRequestDtos);


        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<Food> foods = truckService.getById(truckIds.get(0)).getFoods();
        foods.forEach(food -> {
            UpdateResultDto deleteResult = foodService.deleteFood(truckIds.get(0), food.getId());
            assertThat("푸드 엔티티 삭제", deleteResult.getResult(), is("UPDATED"));
            assertThat("푸드 이미지 삭제", s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckIds.get(0) + "/" + food.getId()), is(false));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(truckService.getById(truckIds.get(0)).getFoods(), hasSize(0));
    }

    @Test
    void testSortFoods() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<String> createdIds = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
            foodSaveRequestDto.setName("food" + i);
            foodSaveRequestDto.setCost(100);
            foodSaveRequestDto.setDescription("this is food" + i);
            foodSaveRequestDto.setImage(null);

            UpdateResultDto updateResultDto = foodService.saveFood(truckIds.get(0), foodSaveRequestDto);
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

        foodService.sortFoods(truckIds.get(0), createdIds);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<Food> foods = truckService.getById(truckIds.get(0)).getFoods();
        List<String> sortedIds = foods.stream().map(food -> food.getId()).collect(Collectors.toList());
        assertThat(sortedIds, equalTo(createdIds));
    }

    private List<TruckSaveRequestDto> createTestTruckData() {

        byte[] imageBinary1 = new byte[128];
        new Random().nextBytes(imageBinary1);
        MockMultipartFile image1 = new MockMultipartFile("image1", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary1);

        TruckSaveRequestDto dto1 = new TruckSaveRequestDto();
        dto1.setName("truck1");
        dto1.setDescription("this is truck1");
        dto1.setUserId("user1");
        dto1.setImage(image1);

        byte[] imageBinary2 = new byte[128];
        new Random().nextBytes(imageBinary2);
        MockMultipartFile image2 = new MockMultipartFile("image2", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary2);

        TruckSaveRequestDto dto2 = new TruckSaveRequestDto();
        dto2.setName("truck2");
        dto2.setDescription("this is truck2");
        dto2.setUserId("user2");
        dto2.setImage(image2);

        return Arrays.asList(dto1, dto2);
    }

    private List<String> indexTestTruckData(List<TruckSaveRequestDto> dtos) {

        List<String> truckIds = new ArrayList<>();
        dtos.forEach(dto -> {
            IndexUpdateResultDto indexResult = truckService.saveTruck(dto);
            log.info("truck index result: " + indexResult.getResult() + ", truck id: " + indexResult.getId());
            assertThat(indexResult.getResult(), is("CREATED"));

            truckIds.add(indexResult.getId());
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        return truckIds;
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
