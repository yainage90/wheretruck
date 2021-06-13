package com.gamakdragons.wheretruck.cloud.aws.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadImage(String bucketName, String fileName, MultipartFile imageFile);
    boolean deleteImage(String bucketName, String fileName);
}
