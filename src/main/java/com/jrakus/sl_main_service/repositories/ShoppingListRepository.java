package com.jrakus.sl_main_service.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jrakus.sl_main_service.configuration.DynamoDBTableConfig;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItemsPerCategoryInner;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ShoppingListRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    ObjectMapper objectMapper = new ObjectMapper();
    private final String pkPrefix = "USER#";
    private final String skPrefix = "SHOPPING_LIST#";

    public ShoppingListRepository(
            DynamoDbClient dynamoDbClient,
            DynamoDBTableConfig dynamoDBTableConfig
    ) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDBTableConfig.getTableName();
    }

    public List<ShoppingList> getAllShoppingListsForUser(String userId) {

        String pkValue = this.pkPrefix + userId;

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":pkVal", AttributeValue.builder().s(pkValue).build());
        expressionValues.put(":skPrefix", AttributeValue.builder().s(this.skPrefix).build());

        String keyConditionExpression = "PK = :pkVal AND begins_with(SK, :skPrefix)";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        List<Map<String, AttributeValue>> items = queryResponse.items();
        List<ShoppingList> shoppingLists = new ArrayList<>();

        queryResponse.items().stream()
                .map(item -> {

                    ShoppingList shoppingList = new ShoppingList();

                    List<ShoppingListItemsPerCategoryInner> itemsPerCategory = objectMapper.readValue(

                    shoppingList.setName(item.get("name").s());
                    shoppingList.setCreatedAt(item.get("createdAt").s());
                    shoppingList.setUpdatedAt(item.get("updatedAt").s());
                    shoppingList.setItemsPerCategory(item.get("itemsPerCategory"));

                    return shoppingList;

                })
                .toList();
    }

}
