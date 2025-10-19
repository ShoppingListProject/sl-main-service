package com.jrakus.sl_main_service.repositories.dynamo_db.utils;

import com.jrakus.sl_main_service.properties.DynamoDBProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamoDBQueryHelper {

    private final DynamoDbClient ddb;
    private final String tableName;

    public DynamoDBQueryHelper(DynamoDbClient dynamoDbClient, DynamoDBProperties dynamoDBProperties) {
        this.ddb = dynamoDbClient;
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

        return ddb.query(queryRequest);
    }

    public List<Map<String, AttributeValue>> getManyItems(String PK, List<String> listOfSK) {

        List<Map<String, AttributeValue>> keys = new ArrayList<>();

        for(String SK: listOfSK) {
            Map<String, AttributeValue> key = Map.of(
                "PK", AttributeValue.builder().s(PK).build(),
                "SK", AttributeValue.builder().s(SK).build()
            );

            keys.add(key);
        }

        Map<String, KeysAndAttributes> requestItems = new HashMap<>();
        requestItems.put(tableName, KeysAndAttributes.builder()
                .keys(keys)
                .build());

        BatchGetItemRequest request = BatchGetItemRequest.builder()
                .requestItems(requestItems)
                .build();

        BatchGetItemResponse response = ddb.batchGetItem(request);

        return response.responses().get(tableName);
    }

    public PutItemResponse saveSingleItem(Map<String, AttributeValue> item) {

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        return ddb.putItem(putRequest);
    }
}
