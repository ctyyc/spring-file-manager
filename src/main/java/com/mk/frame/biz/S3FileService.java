package com.mk.frame.biz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3FileService {
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

	private final S3Client s3Client;

    /**
     * 파일 업로드
     * @param filePath
     * @param keyName
     */
    public void uploadFile(String filePath, String keyName) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build(),
                    Paths.get(filePath)
            );
            log.info("=== uploadFile Success : {}", keyName);
        } catch (S3Exception e) {
            log.error("=== uploadFile Error : {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 파일 다운로드
     *
     * @param downloadPath
     * @param keyName
     * @return
     */
    public GetObjectResponse downloadFile(String downloadPath, String keyName) {
        try {
            return s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build(),
                    Paths.get(downloadPath)
            );
        } catch (S3Exception e) {
            log.error("=== downloadFile Error : {}", e.getMessage());
            throw e;
        }
    }
}
