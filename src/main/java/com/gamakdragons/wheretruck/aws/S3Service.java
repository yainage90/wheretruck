package com.gamakdragons.wheretruck.aws;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    String uploadFoodImage(String truckId, String foodId, MultipartFile imageFile);
    boolean deleteFoodImage(String truckId, String foodId);
    String getFoodImageUrl(String truckId, String foodId);

    void createS3Bucket();
    void deleteS3Bucket();
}
