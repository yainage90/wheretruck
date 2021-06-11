package com.gamakdragons.wheretruck.cloud.aws.controller;

import java.util.stream.Collectors;

import com.amazonaws.services.s3.AmazonS3;
import com.gamakdragons.wheretruck.cloud.aws.entity.S3Status;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3MonitorController {

    private final AmazonS3 s3Client;

    @GetMapping("/status")
    public ResponseEntity<S3Status> status() {

        S3Status status = S3Status.builder()
                                    .regionName(s3Client.getRegionName())
                                    .accountOwner(s3Client.getS3AccountOwner().getId())
                                    .bucketList(s3Client.listBuckets().stream().map(bucket -> bucket.getName()).collect(Collectors.toList()))
                                    .build();

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
    
}
