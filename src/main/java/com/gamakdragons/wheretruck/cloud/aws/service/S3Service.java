package com.gamakdragons.wheretruck.cloud.aws.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    String uploadFoodImage(String bucketName, String truckId, String foodId, MultipartFile imageFile);
    boolean deleteFoodImage(String bucketName, String truckId, String foodId);
}
