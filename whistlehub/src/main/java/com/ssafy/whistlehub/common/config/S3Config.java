package com.ssafy.whistlehub.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * <pre>AWS S3 연동</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-13
 */

@Configuration
public class S3Config {
    @Value("${AWS_S3_ACCESS}")
    private String accessKey;
    @Value("${AWS_S3_SECRET}")
    private String secretKey;
    @Value("${AWS_S3_REGION}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder().region(Region.AP_NORTHEAST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}

