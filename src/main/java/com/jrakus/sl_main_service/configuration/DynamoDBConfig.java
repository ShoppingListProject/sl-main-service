package com.jrakus.sl_main_service.configuration;

import com.jrakus.sl_main_service.properties.AWSProperties;
import com.jrakus.sl_main_service.properties.DynamoDBProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class DynamoDBConfig {

    private final AWSProperties awsProperties;
    private final DynamoDBProperties dynamoDBProperties;

    public DynamoDBConfig(AWSProperties awsProperties, DynamoDBProperties dynamoDBProperties) {
        this.awsProperties = awsProperties;
        this.dynamoDBProperties = dynamoDBProperties;
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {

        StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        this.awsProperties.getAccessKeyId(),
                        this.awsProperties.getSecretKey()
                )
        );

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(this.dynamoDBProperties.getEndpoint()))
                .region(Region.of(this.awsProperties.getRegion()))
                .credentialsProvider(staticCredentialsProvider)
                .build();
    }
}
