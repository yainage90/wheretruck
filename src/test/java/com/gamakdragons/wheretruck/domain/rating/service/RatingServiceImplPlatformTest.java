package com.gamakdragons.wheretruck.domain.rating.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gamakdragons.wheretruck.TestIndexUtil;
import com.gamakdragons.wheretruck.cloud.aws.service.S3ServiceImpl;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.config.S3Config;
import com.gamakdragons.wheretruck.domain.rating.dto.MyRatingDto;
import com.gamakdragons.wheretruck.domain.rating.entity.Rating;
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

@SpringBootTest(classes = {RatingServiceImpl.class, TruckServiceImpl.class, ElasticSearchTestConfig.class, TestIndexUtil.class, S3ServiceImpl.class, S3Config.class}, 
                properties = {"spring.config.location=classpath:application-test.yml"})
@Slf4j
public class RatingServiceImplPlatformTest {

    @Autowired
    private TruckService truckService;

    @Autowired
    private RatingService ratingService;
    
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
    public void beforeEach() throws IOException, InterruptedException {
        TestIndexUtil.initRestHighLevelClient();
        TestIndexUtil.deleteTestTruckIndex();
        TestIndexUtil.createTestTruckIndex();
    }

    @AfterEach
    public void afterEach() throws IOException {
        TestIndexUtil.deleteTestTruckIndex();
    }

    @Test
    void testSaveRatingUpdateTruckNumRatingAndStarAvg() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();

        ratings.forEach(rating -> {
            UpdateResultDto indexResult = ratingService.saveRating(truckIds.get(0), rating);
            assertThat(indexResult.getResult(), is("UPDATED"));
            assertThat(indexResult.getId(), is(rating.getId()));
        });

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(truckIds.get(0));
        List<Rating> truck1Ratings = truck.getRatings();

        assertThat(truck.getNumRating(), is(ratings.size()));
        assertThat(truck1Ratings.size(), is(ratings.size()));

        double calculatedStarAvg = ratings.stream().mapToDouble(rating -> rating.getStar()).average().getAsDouble();
        assertThat((double) truck.getStarAvg(), closeTo(calculatedStarAvg, 0.0001f));
    }

    @Test
    void testUpdateRatingUpdateTruckStarAvg() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(truckIds.get(0), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        float starToUpdate = ratings.get(0).getStar() + 1.0f;
        String commentToUpdate = "정말 재밌어요ㅋㅋ";
        ratings.get(0).setStar(starToUpdate);
        ratings.get(0).setComment(commentToUpdate);
        UpdateResultDto updateResult = ratingService.saveRating(truckIds.get(0), ratings.get(0));

        assertThat(updateResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(truckIds.get(0));
        double calculatedStarAvg = ratings.stream().mapToDouble(rating -> rating.getStar()).average().getAsDouble();
        assertThat((double) truck.getStarAvg(), closeTo(calculatedStarAvg, 0.0001f));
    }

    @Test
    void testDeleteRating() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(truckIds.get(0), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        UpdateResultDto deleteResult = ratingService.deleteRating(truckIds.get(0), ratings.get(0).getId());
        assertThat(deleteResult.getResult(), is("UPDATED"));

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        Truck truck = truckService.getById(truckIds.get(0));
        List<Rating> truck1Ratings = truck.getRatings();

        assertThat(truck.getNumRating(), is(ratings.size() - 1));
        assertThat(truck1Ratings.size(), is(ratings.size() - 1));
        assertThat(truck1Ratings, hasItems(ratings.get(1), ratings.get(2)));
        assertThat(truck1Ratings, not(hasItem(ratings.get(0))));

        assertThat((double) truck.getStarAvg(), closeTo((ratings.get(1).getStar() + ratings.get(2).getStar()) / 2, 0.0001f));
    }

    @Test
    void testFindByUserIdResultIsInCreatedDateReverseOrder() {

        List<TruckSaveRequestDto> trucks = createTestTruckData();
        List<String> truckIds = indexTestTruckData(trucks);

        List<Rating> ratings = createTestRatingData();
        indexTestRatingData(truckIds.get(0), ratings);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        String commonUserId = ratings.get(0).getUserId();

        List<MyRatingDto> searchedRatings = ratingService.findByUserId(commonUserId).getDocs();
        assertThat(searchedRatings.stream().map(r -> r.getId()).collect(Collectors.toList()), 
                    contains(ratings.get(2).getId(), ratings.get(1).getId(), ratings.get(0).getId())
        );
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
            Thread.sleep(1000);
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
}
