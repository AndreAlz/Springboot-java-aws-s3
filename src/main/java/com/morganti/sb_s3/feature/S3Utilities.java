package com.morganti.sb_s3.feature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Date;

@Service
public class S3Utilities {

    private final S3Client clientS3;
    private final S3Presigner s3presigner;

    public S3Utilities(S3Client clientS3, S3Presigner s3presigner) {
        this.clientS3 = clientS3;
        this.s3presigner = s3presigner;
    }

    public boolean doesBucketExist(String bucketName) {
        HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        try {
            HeadBucketResponse response = clientS3.headBucket(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createBucket(String bucketName) {
        if (doesBucketExist(bucketName)) {
            throw new RuntimeException("Bucket already exists");
        }
        CreateBucketRequest request = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        CreateBucketResponse response = clientS3.createBucket(request);
        System.out.println("Bucket created: " + response.location());
    }

    public PutObjectResponse uploadFileToS3(String bucketName, String key, byte[] file) {
        if (!doesBucketExist(bucketName)) {
            throw new RuntimeException("Bucket does not exist");
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        PutObjectResponse result = clientS3.putObject(request, RequestBody.fromBytes(file));
        return result;
    }

    public InputStreamResource downloadFileFromS3(String bucketName, String key) {
        if (!doesBucketExist(bucketName)) {
            throw new RuntimeException("Bucket does not exist");
        }
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> response = clientS3.getObject(request);
        return new InputStreamResource(response);
    }

    public String preSigneUrl (String bucketName,String key){


        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(getObjectRequest)
                .build();
        PresignedGetObjectRequest presignedGetObjectRequest = s3presigner.presignGetObject(getObjectPresignRequest);
        String url = presignedGetObjectRequest.url().toString();

        return url;
    }
}
