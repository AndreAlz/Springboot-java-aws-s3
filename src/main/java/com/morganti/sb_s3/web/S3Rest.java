package com.morganti.sb_s3.web;

import com.morganti.sb_s3.feature.S3Utilities;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;

@RestController
public class S3Rest {
    private final S3Utilities s3Utilities;

    public S3Rest(S3Utilities s3Utilities) {
        this.s3Utilities = s3Utilities;
    }

    @GetMapping("/download/{bucketName}/{key}")
    public  ResponseEntity<String> downloadFileFromS3(@PathVariable String bucketName, @PathVariable String key) throws IOException {
        //InputStreamResource result = s3Utilities.downloadFileFromS3(bucketName, key);
        String url = s3Utilities.preSigneUrl(bucketName,key);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(url);
    }

    @PostMapping("/upload/{bucketName}")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String bucketName) throws IOException {
        String fileName = file.getOriginalFilename();
        byte[] fileContent = file.getBytes();
        PutObjectResponse response = s3Utilities.uploadFileToS3(bucketName,fileName,  fileContent);

        String message = "File uploaded successfully with URL: " + String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }
}