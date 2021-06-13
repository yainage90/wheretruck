package com.gamakdragons.wheretruck.cloud.aws.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.gamakdragons.wheretruck.cloud.aws.exception.S3ServiceException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    
    private final AmazonS3 s3Client;

    @Override
    public String uploadImage(String bucketName, String fileName, MultipartFile imageFile) throws S3ServiceException {

        if (!s3Client.doesBucketExistV2(bucketName)) {
            s3Client.createBucket(new CreateBucketRequest(bucketName));
            if(s3Client.doesBucketExistV2(bucketName)) {
                log.info("bucket created: " + bucketName);
            } else {
                log.error("bucket create failed: " + bucketName);
            }
        }

        if(imageFile == null) {
            return null;
        }

        File file;
        try {
            file = convertToFile(imageFile);
        } catch(IOException e) {
            throw new S3ServiceException("Failed to convert file");
        }

        try {
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch(AmazonServiceException e) {
            log.error("AmazonServiceException occured", e);
            throw new S3ServiceException("AmazonServiceException");
        } catch(SdkClientException e) {
            log.error("SdkClientException", e);
            throw new S3ServiceException("SdkClientException");
        }

        file.delete();
        return getFoodImageUrl(bucketName, fileName);
    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File file = Files.createTempFile(UUID.randomUUID().toString(), ".tmp").toFile();

        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        } catch(IOException e) {
            log.error(e.getMessage());
            return null;
        }

        return file;
    }

    @Override
    public boolean deleteImage(String bucketName, String fileName) throws S3ServiceException {
       
        try {
            if(s3Client.doesObjectExist(bucketName, fileName)) {
                s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            }

            return !s3Client.doesObjectExist(bucketName, fileName);
        } catch(SdkClientException e) {
            log.error(e.getMessage());
            throw new S3ServiceException(e.getMessage());
        }
    }

    private String getFoodImageUrl(String bucketName, String fileName) {
        return s3Client.getUrl(bucketName, fileName).toString();
    }

    @Override
    public void deleteImagesWithPrefix(String bucketName, String prefix) throws S3ServiceException {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                                                    .withBucketName(bucketName)
                                                    .withPrefix(prefix);
        try {
            ListObjectsV2Result listObjectsV2Result = s3Client.listObjectsV2(listObjectsV2Request);

            for (S3ObjectSummary objectSummary : listObjectsV2Result.getObjectSummaries()) {
                s3Client.deleteObject(bucketName, objectSummary.getKey());
            }
        } catch(SdkClientException e) {
            log.error(e.getMessage(), e);
            throw new S3ServiceException(e.getMessage());
        }
    }
}
