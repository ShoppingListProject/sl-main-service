package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.RecipesRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import java.util.List;

@Repository
public class RecipesRepositoryDynamoDB implements RecipesRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;

    private final String pkGlobal = "GLOBAL#RECIPES";

    private final String pkPrefix = "USER#";
    private final String skPrefix = "RECIPE#";

    public RecipesRepositoryDynamoDB(DynamoDBQueryHelper dynamoDBQueryHelper) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
    }

    @Override
    public List<Recipe> getAllPublicRecipes() {

        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(this.pkGlobal, this.skPrefix);

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
