package com.gamakdragons.wheretruck.domain.truck.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gamakdragons.wheretruck.TestIndexUtil;
import com.gamakdragons.wheretruck.cloud.aws.service.S3ServiceImpl;
import com.gamakdragons.wheretruck.cloud.elasticsearch.service.ElasticSearchServiceImpl;
import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.GeoLocation;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.S3Config;
import com.gamakdragons.wheretruck.domain.favorite.entity.Favorite;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteService;
import com.gamakdragons.wheretruck.domain.favorite.service.FavoriteServiceImpl;
import com.gamakdragons.wheretruck.domain.food.dto.FoodSaveRequestDto;
import com.gamakdragons.wheretruck.domain.food.service.FoodService;
import com.gamakdragons.wheretruck.domain.food.service.FoodServiceImpl;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
import com.gamakdragons.wheretruck.domain.rating.service.RatingService;
import com.gamakdragons.wheretruck.domain.rating.service.RatingServiceImpl;
import com.gamakdragons.wheretruck.domain.truck.dto.TruckSaveRequestDto;
import com.gamakdragons.wheretruck.domain.truck.entity.Truck;
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
    classes = {TruckServiceImpl.class, ElasticSearchServiceImpl.class, ElasticSearchTestConfig.class,
                RatingServiceImpl.class, S3ServiceImpl.class, S3Config.class, FoodServiceImpl.class, FavoriteServiceImpl.class, TestIndexUtil.class}, 
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class TruckServiceTest {

    @Autowired
    private TruckService truckService;

    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private FoodService foodService;
    
    @Value("${elasticsearch.index.truck.name}")
    private String TEST_TRUCK_INDEX;

    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket.truck_image}")
    private String TRUCK_IMAGE_BUCKET;

    @Value("${cloud.aws.s3.bucket.food_image}")
    private String FOOD_IMAGE_BUCKET;
   
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
        TestIndexUtil.deleteTestIndices();
        TestIndexUtil.createTestIndices();
        initS3Client();
    }

    @AfterEach
    public void afterEach() throws IOException {
        TestIndexUtil.deleteTestIndices();

        deleteAllBucketObjects();
        deleteS3Bucket(TRUCK_IMAGE_BUCKET);
    }

    @Test
    void testFindAll() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(dtos);

        SearchResultDto<Truck> result = truckService.findAll();

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(dtos.size()));
        List<String> retrievedIds = result.getDocs().stream().map(truck -> truck.getId()).collect(Collectors.toList());

        //check ids
        truckIds.forEach(truckId -> {
            assertThat(retrievedIds, hasItem(truckId));
        });

        //check name, description
        dtos.forEach(dto -> {
            assertThat(result.getDocs(), hasItem(
                allOf(
                    hasProperty("name", is(dto.getName())),
                    hasProperty("description", is(dto.getDescription()))
                )
            ));
        });

        assertThat(result.getDocs(), hasItem(hasProperty("imageUrl", is(nullValue()))));
        assertThat(result.getDocs(), hasItem(hasProperty("imageUrl", is(not(nullValue())))));
    }

    @Test
    void testFindByGeoLocation() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(dtos);


        GeoLocation geo1 = new GeoLocation(30.0f, 130.0f);
        truckService.openTruck(truckIds.get(0), geo1);
        GeoLocation geo2 = new GeoLocation(40.0f, 140.0f);
        truckService.openTruck(truckIds.get(1), geo2);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        SearchResultDto<Truck> result = truckService.findByGeoLocation(geo1, 10.0f); //10km

        assertThat(result.getStatus(), is("OK"));
        assertThat(result.getNumFound(), is(1));
        assertThat(result.getDocs().get(0).getId(), is(truckIds.get(0)));
    }

    @Test
    void testFindByUserId() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();
        indexTestTruckData(dtos);

        String userIdToFind = dtos.get(0).getUserId();

        SearchResultDto<Truck> result = truckService.findByUserId(userIdToFind);
        log.info(result.toString());
        
        assertThat(result.getStatus(), is("OK"));

        int dtoCountWithUserId = (int) dtos.stream().filter(dto -> dto.getUserId().equals(userIdToFind)).count();
        assertThat(result.getNumFound(), is(dtoCountWithUserId));
        result.getDocs().forEach(truck -> {
            assertThat(truck.getUserId(), is(userIdToFind));
        });
    }

    @Test
    void testFindByUserIdRatingsIsInCreatedDateReverseOrder() {

        List<TruckSaveRequestDto> trucks = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(truckIds.get(0), ratings);

        SearchResultDto<Truck> result = truckService.findByUserId(trucks.get(0).getUserId());
        log.info(result.toString());
        
        result.getDocs().stream().forEach(truck -> {
            assertThat(truck.getRatings().stream().map(rating -> rating.getId()).collect(Collectors.toList()),
                        contains(ratings.get(2).getId(), ratings.get(1).getId(), ratings.get(0).getId())
            );
        });

    }

    @Test
    void testGetById() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(dtos);

        Truck truck = truckService.getById(truckIds.get(0));
        log.info(truck.toString());

        assertThat(truck.getId(), is(truckIds.get(0)));
        assertThat(truck.getName(), is(dtos.get(0).getName()));
        assertThat(truck.getDescription(), is(dtos.get(0).getDescription()));
        assertThat(truck.getImageUrl(), not(nullValue()));
    }

    @Test
    void testGetByIdRatingsIsInCreatedDateReverseOrder() {

        List<TruckSaveRequestDto> trucks = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(truckIds.get(0), ratings);

        Truck truck = truckService.getById(truckIds.get(0));
        log.info(truck.toString());

        assertThat(truck.getRatings().stream().map(rating -> rating.getId()).collect(Collectors.toList()),
                    contains(ratings.get(2).getId(), ratings.get(1).getId(), ratings.get(0).getId())
        );
    }

    @Test
    void testSaveTruck() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();

        dtos.stream().forEach(dto -> {
            IndexUpdateResultDto indexResult = truckService.saveTruck(dto);
            log.info("truck index result: " + indexResult.getResult() + ", truck id: " + indexResult.getId());

            assertThat(indexResult.getResult(), is("CREATED"));
        });

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        List<Truck> trucks = truckService.findAll().getDocs();

        assertThat(trucks.size(), is(dtos.size()));
        trucks.stream().filter(truck -> truck.getImageUrl() != null).forEach(truck -> {
            assertThat("트럭 이미지 업로드", s3Client.doesObjectExist(TRUCK_IMAGE_BUCKET, truck.getId()), is(true));
        });
    }

    @Test
    void testUpdateTruck() {

        List<TruckSaveRequestDto> truckSaveRequestDtos = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(truckSaveRequestDtos);
        assertThat(s3Client.doesObjectExist(TRUCK_IMAGE_BUCKET, truckIds.get(1)), is(false));

        String nameToUpdate = "updatedTruck2";
        String descriptionToUpdate = "this is updated truck2";

        TruckSaveRequestDto truckUpdateRequestDto = new TruckSaveRequestDto();
        truckUpdateRequestDto.setId(truckIds.get(1));
        truckUpdateRequestDto.setName(nameToUpdate);
        truckUpdateRequestDto.setDescription(descriptionToUpdate);

        byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);
        truckUpdateRequestDto.setImage(image);

        IndexUpdateResultDto updateResult = truckService.updateTruck(truckUpdateRequestDto);
        assertThat(updateResult.getResult(), equalTo("UPDATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck updatedTruck = truckService.getById(truckIds.get(1));
        assertThat(updatedTruck.getName(), equalTo(nameToUpdate));
        assertThat(updatedTruck.getDescription(), equalTo(descriptionToUpdate));
        assertThat(updatedTruck.getImageUrl(), is(not(nullValue())));
        assertThat(s3Client.doesObjectExist(TRUCK_IMAGE_BUCKET, updatedTruck.getId()), is(true));
    }

    @Test
    void testDeleteTruck() {

        List<TruckSaveRequestDto> dtos = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(dtos);

        DeleteResultDto deleteResult1 = truckService.deleteTruck(truckIds.get(0));
        assertThat(deleteResult1.getResult(), is("DELETED"));

        assertThat(truckService.getById(truckIds.get(0)), nullValue());
        assertThat(truckService.getById(truckIds.get(1)), not(nullValue()));

        assertThat("트럭 이미지 삭제", s3Client.doesObjectExist(TRUCK_IMAGE_BUCKET, truckIds.get(0)), is(false));
    }

    @Test
    void testDeleteFoodImagesInS3BucketIfTruckDeleted() {

        TruckSaveRequestDto dto = createTestTruckSaveRequestDtos().get(0);
        IndexUpdateResultDto indexResult = truckService.saveTruck(dto);
        String truckId = indexResult.getId();

        assertThat(indexResult.getResult(), is("CREATED"));

        try {
            Thread.sleep(1500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        FoodSaveRequestDto foodSaveRequestDto = new FoodSaveRequestDto();
        foodSaveRequestDto.setName("food");
        foodSaveRequestDto.setCost(10000);
        foodSaveRequestDto.setDescription("this is food");
        byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile image = new MockMultipartFile("image", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);
        foodSaveRequestDto.setImage(image);
        
        UpdateResultDto updateResultDto = foodService.saveFood(truckId, foodSaveRequestDto);
        String foodId = updateResultDto.getId();

        assertThat(updateResultDto.getResult(), is("UPDATED"));
        assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckId + "/" + foodId), is(true));

        try {
            Thread.sleep(1500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(truckService.deleteTruck(truckId).getResult(), is("DELETED"));
        assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckId + "/" + foodId), is(false));
    }

    @Test
    void testDeleteFavoritesWhenTruckDeleted() {

        TruckSaveRequestDto dto = createTestTruckSaveRequestDtos().get(0);
        IndexUpdateResultDto indexResult = truckService.saveTruck(dto);
        String truckId = indexResult.getId();

        assertThat(indexResult.getResult(), is("CREATED"));

        

        Favorite favorite = new Favorite();
        favorite.setTruckId(truckId);
        favorite.setUserId(UUID.randomUUID().toString());

        assertThat(favoriteService.saveFavorite(favorite).getResult(), is("CREATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(favoriteService.findByTruckId(truckId).getNumFound(), is(1));

        assertThat(truckService.deleteTruck(truckId).getResult(), is("DELETED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        assertThat(favoriteService.findByTruckId(truckId).getNumFound(), is(0));
    }

    @Test
    void testOpenTruck() {

        List<TruckSaveRequestDto> testTrucks = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(testTrucks);
        
        GeoLocation openLocation = new GeoLocation(33.0f, 133.0f);
        IndexUpdateResultDto updateResult = truckService.openTruck(truckIds.get(0), openLocation);
        assertThat(updateResult.getResult(), is("UPDATED"));

    try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck startedTruck = truckService.getById(truckIds.get(0));
        assertThat(startedTruck.isOpened(), is(true));
        assertThat((double) startedTruck.getGeoLocation().getLat(), closeTo(openLocation.getLat(), 0.001f));
        assertThat((double) startedTruck.getGeoLocation().getLon(), closeTo(openLocation.getLon(), 0.001f));
    }

    @Test
    void testStopTruck() {

        List<TruckSaveRequestDto> testTrucks = createTestTruckSaveRequestDtos();
        List<String> truckIds = indexTestTruckData(testTrucks);
        
        GeoLocation openLocation = new GeoLocation(33.0f, 133.0f);
        IndexUpdateResultDto updateResult = truckService.openTruck(truckIds.get(0), openLocation);
        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        IndexUpdateResultDto stopResult = truckService.stopTruck(truckIds.get(0));
        assertThat(stopResult.getResult(), is("UPDATED"));

        Truck stoppedTruck = truckService.getById(truckIds.get(0));
        assertThat(stoppedTruck.isOpened(), is(false));
    }

    private List<TruckSaveRequestDto> createTestTruckSaveRequestDtos() {

        byte[] imageBinary1 = new byte[128];
        new Random().nextBytes(imageBinary1);
        MockMultipartFile image1 = new MockMultipartFile("image1", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary1);

        TruckSaveRequestDto dto1 = new TruckSaveRequestDto();
        dto1.setName("truck1");
        dto1.setDescription("this is truck1");
        dto1.setUserId("user1");
        dto1.setImage(image1);

        TruckSaveRequestDto dto2 = new TruckSaveRequestDto();
        dto2.setName("truck2");
        dto2.setDescription("this is truck2");
        dto2.setUserId("user2");

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

    private List<Rating> createTestRatingData() {

        String userId = UUID.randomUUID().toString();
        
        Rating rating1 = new Rating();
        rating1.setUserId(userId);
        rating1.setComment("hello1");
        rating1.setStar(3.0f);

        Rating rating2 = new Rating();
        rating2.setUserId(userId);
        rating2.setComment("hello2");
        rating2.setStar(5.0f);

        Rating rating3 = new Rating();
        rating3.setUserId(userId);
        rating3.setComment("hello3");
        rating3.setStar(1.0f);

        return Arrays.asList(rating1, rating2, rating3);
    }

    private void indexTestRatingData(String truckId, List<Rating> ratings) {

        ratings.forEach(rating-> {
            UpdateResultDto updateResult = ratingService.saveRating(truckId, rating);
            log.info("rating index result: " + updateResult.getResult());
            assertThat(updateResult.getResult(), is("UPDATED"));

            try {
                Thread.sleep(2000);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
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

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllBucketObjects() {

        try {
            ObjectListing objectListing = s3Client.listObjects(TRUCK_IMAGE_BUCKET);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    s3Client.deleteObject(TRUCK_IMAGE_BUCKET, objIter.next().getKey());
                }

                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }

            s3Client.deleteBucket(TRUCK_IMAGE_BUCKET);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    

}
