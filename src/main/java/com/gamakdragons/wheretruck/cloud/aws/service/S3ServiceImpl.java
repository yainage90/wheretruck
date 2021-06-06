package com.gamakdragons.wheretruck.cloud.aws.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    
    private final AmazonS3 s3Client;

    @Override
    public String uploadFoodImage(String bucketName, String truckId, String foodId, MultipartFile imageFile) {

        String fileName = truckId + foodId;
        File foodImageFile = convertToFile(fileName, imageFile);
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, foodImageFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return getFoodImageUrl(bucketName, truckId, foodId);
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
    public boolean deleteFoodImage(String bucketName, String truckId, String foodId) {
        String fileName = truckId + foodId;
        if(s3Client.doesObjectExist(bucketName, fileName)) {
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        }

        return !s3Client.doesObjectExist(bucketName, fileName);
    }

    private String getFoodImageUrl(String bucketName, String truckId, String foodId) {
        String fileName = truckId + foodId;
        return s3Client.getUrl(bucketName, fileName).toString();
    }
}
