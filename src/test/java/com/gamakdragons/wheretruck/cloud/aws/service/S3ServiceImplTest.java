package com.gamakdragons.wheretruck.cloud.aws.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gamakdragons.wheretruck.config.S3Config;

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
    classes = {S3ServiceImpl.class, S3Config.class}, 
    properties = {"spring.config.location=classpath:application-test.yml"}
)
@Slf4j
public class S3ServiceImplTest {

    @Value("${cloud.aws.s3.bucket.food_image}")
    private String FOOD_IMAGE_BUCKET;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    private AmazonS3 s3Client;

    @Autowired
    private S3Service service;

    @BeforeEach
    public void setup() {
        initS3Client();
        createS3Bucket(FOOD_IMAGE_BUCKET);
    }

    @AfterEach
    public void tearDown() {
        deleteAllBucketObjects(FOOD_IMAGE_BUCKET);
        deleteS3Bucket(FOOD_IMAGE_BUCKET);
    }

    @Test
    void testUploadFoodImage() throws IOException {

        String truckId = UUID.randomUUID().toString();
        String foodId = UUID.randomUUID().toString();

        byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile imageFile = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);
        String imageUrl = service.uploadFoodImage(FOOD_IMAGE_BUCKET, truckId, foodId, imageFile);
        log.info("image uploaded. url="+ imageUrl);

        assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckId + foodId), is(true));

        byte[] readImageBinary = new byte[128];
        s3Client.getObject(new GetObjectRequest(FOOD_IMAGE_BUCKET, truckId + foodId)).getObjectContent().read(readImageBinary);
        assertThat(readImageBinary, is(imageBinary));
    }

    @Test
    void testDeleteFoodImage() {

        String truckId = UUID.randomUUID().toString();
        String foodId = UUID.randomUUID().toString();

        byte[] imageBinary = new byte[128];
        new Random().nextBytes(imageBinary);
        MockMultipartFile imageFile = new MockMultipartFile("file", null, MediaType.MULTIPART_FORM_DATA_VALUE, imageBinary);
        String imageUrl = service.uploadFoodImage(FOOD_IMAGE_BUCKET, truckId, foodId, imageFile);
        log.info("image uploaded. url="+ imageUrl);

        assertThat(s3Client.doesObjectExist(FOOD_IMAGE_BUCKET, truckId + foodId), is(true));
        assertThat(service.deleteFoodImage(FOOD_IMAGE_BUCKET, truckId, foodId), is(true));
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

    private void deleteAllBucketObjects(String bucketName) {

        try {
            ObjectListing objectListing = s3Client.listObjects(bucketName);
            while (true) {
                Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
                while (objIter.hasNext()) {
                    s3Client.deleteObject(bucketName, objIter.next().getKey());
                }

                if (objectListing.isTruncated()) {
                    objectListing = s3Client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }

            s3Client.deleteBucket(bucketName);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }
}
