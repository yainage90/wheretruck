package com.gamakdragons.wheretruck.aws;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    
    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket.food_image}")
    private String foodImageBucket;

    @Override
    public String uploadFoodImage(String truckId, String foodId, MultipartFile imageFile) {

        String fileName = truckId + foodId;
        File foodImageFile = convertToFile(fileName, imageFile);
        s3Client.putObject(new PutObjectRequest(foodImageBucket, fileName, foodImageFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return getFoodImageUrl(truckId, foodId);
    }

    private File convertToFile(String fileName, MultipartFile multipartFile) {
        File file = new File(fileName);

        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch(IOException e) {
            log.error(e.getMessage());
            return null;
        }

        return file;
    }

    @Override
    public boolean deleteFoodImage(String truckId, String foodId) {
        String fileName = truckId + foodId;
        if(s3Client.doesObjectExist(foodImageBucket, fileName)) {
            s3Client.deleteObject(new DeleteObjectRequest(foodImageBucket, fileName));
        }

        return s3Client.doesObjectExist(foodImageBucket, fileName);
    }

    @Override
    public String getFoodImageUrl(String truckId, String foodId) {
        String fileName = truckId + foodId;
        return s3Client.getUrl(foodImageBucket, fileName).toString();
    }

    @Override
    public void createS3Bucket() {

        if (!s3Client.doesBucketExistV2(foodImageBucket)) {
            s3Client.createBucket(new CreateBucketRequest(foodImageBucket));
            if(s3Client.doesBucketExistV2(foodImageBucket)) {
                log.info("bucket created: " + foodImageBucket);
            } else {
                log.error("bucket create failed: " + foodImageBucket);
            }
        }

        log.info("bucket exists: " + foodImageBucket);
    }

    @Override
    public void deleteS3Bucket() {
        if(s3Client.doesBucketExistV2(foodImageBucket)) {
            s3Client.deleteBucket(new DeleteBucketRequest(foodImageBucket));
            if(!s3Client.doesBucketExistV2(foodImageBucket)) {
                log.info("bucket deleted: " + foodImageBucket);
            } else {
                log.error("bucket exists but delete bucket failed: " + foodImageBucket);
            }
        }

        log.info("bucket does not exist: " + foodImageBucket);
    }
}
