package com.jrakus.sl_main_service.repositories.dynamo_db.utils;

import com.jrakus.sl_main_service.properties.DynamoDBProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Component
public class DynamoDBQueryHelper {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDBQueryHelper(DynamoDbClient dynamoDbClient, DynamoDBProperties dynamoDBProperties) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDBProperties.getTableName();
    }

    public QueryResponse queryUsingPKAndSKPrefix(String PK, String SKPrefix) {

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":pk", AttributeValue.builder().s(PK).build());
        expressionValues.put(":skPrefix", AttributeValue.builder().s(SKPrefix).build());

        String keyConditionExpression = "PK = :pk AND begins_with(SK, :skPrefix)";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionValues)
                .build();

        return dynamoDbClient.query(queryRequest);
    }

    public PutItemResponse saveSingleItem(Map<String, AttributeValue> item) {

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        return dynamoDbClient.putItem(putRequest);
    }
}
