package com.jrakus.sl_main_service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDBConfig {

    private final String dynamoDbEndpoint;
    private final String region;
    private final String accessKeyId;
    private final String secretKey;

    public DynamoDBConfig(
            @Value("${dynamodb.endpoint}") String dynamoDbEndpoint,
            @Value("${dynamodb.region}") String region,
            @Value("${aws.accessKeyId}") String accessKeyId,
            @Value("${aws.secretKey}") String secretKey
    ) {
        this.dynamoDbEndpoint = dynamoDbEndpoint;
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.secretKey = secretKey;
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(dynamoDbEndpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretKey)
                        )
                )
                .build();
    }
}
