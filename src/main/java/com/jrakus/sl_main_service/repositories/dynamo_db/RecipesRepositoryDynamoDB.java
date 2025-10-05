package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.configuration.DynamoDBTableConfig;
import com.jrakus.sl_main_service.repositories.RecipesRepository;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesRepositoryDynamoDB implements RecipesRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    private final String pkGlobal = "GLOBAL#RECIPES";

    private final String pkPrefix = "USER#";
    private final String skPrefix = "RECIPE#";

    public RecipesRepositoryDynamoDB(DynamoDbClient dynamoDbClient, DynamoDBTableConfig dynamoDBTableConfig) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = dynamoDBTableConfig.getTableName();
    }

    @Override
    public List<Recipe> getAllPublicRecipes() {

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":pkVal", AttributeValue.builder().s(pkGlobal).build());
        expressionValues.put(":skPrefix", AttributeValue.builder().s(this.skPrefix).build());

        String keyConditionExpression = "PK = :pkVal AND begins_with(SK, :skPrefix)";

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionValues)
                .build();

        QueryResponse queryResponse = dynamoDbClient.query(queryRequest);

        return queryResponse.items()
                .stream()
                .map(recipeDynamoDB -> {

            Recipe recipe = new Recipe();

            recipe.setName(recipeDynamoDB.get("name").s());
            recipe.setCreatedAt(recipeDynamoDB.get("createdAt").s());
            recipe.setUpdatedAt(recipeDynamoDB.get("updatedAt").s());

            List<RecipeItem> items = recipeDynamoDB.get("items").l().stream()
                    .map(AttributeValue::m)
                    .map( itemDynamoDB ->
                         new RecipeItem()
                                .category(itemDynamoDB.get("category").s())
                                .name(itemDynamoDB.get("name").s())
                                .quantity(Integer.valueOf(itemDynamoDB.get("quantity").n()))
                                .unit(itemDynamoDB.get("unit").s())

                    ).toList();

            return recipe;

        }).toList();

    }
}
