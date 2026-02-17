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

    public QueryResponse queryUsingPKAndSKBetween(String pk, String skFrom, String skTo) {
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        expressionValues.put(":pk", AttributeValue.builder().s(pk).build());
        expressionValues.put(":start", AttributeValue.builder().s(skFrom).build());
        expressionValues.put(":end", AttributeValue.builder().s(skTo).build());

        String keyConditionExpression = "PK = :pk AND SK BETWEEN :start AND :end";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionValues)
                .build();

        return ddb.query(queryRequest);
    }

    public QueryResponse queryUsingPKAndSKPrefix(String pk, String skPrefix) {

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":pk", AttributeValue.builder().s(pk).build());
        expressionValues.put(":skPrefix", AttributeValue.builder().s(skPrefix).build());

        String keyConditionExpression = "PK = :pk AND begins_with(SK, :skPrefix)";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionValues)
                .build();

        return ddb.query(queryRequest);
    }

    public List<Map<String, AttributeValue>> getManyItems(String pk, List<String> listOfSk) {

        List<Map<String, AttributeValue>> keys = new ArrayList<>();

        for(String sk: listOfSk) {
            Map<String, AttributeValue> key = Map.of(
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build()
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

    public Map<String, AttributeValue> getSingleItem(String pk, String sk) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("PK", AttributeValue.builder().s(pk).build());
        key.put("SK", AttributeValue.builder().s(sk).build());

        GetItemRequest getRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        return ddb.getItem(getRequest).item();
    }

    public void deleteSingleItem(String pk, String sk) {

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("PK", AttributeValue.builder().s(pk).build());
        key.put("SK", AttributeValue.builder().s(sk).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        ddb.deleteItem(request);
    }

    public void saveSingleItem(Map<String, AttributeValue> item) {

        PutItemRequest putRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        ddb.putItem(putRequest);
    }
}
