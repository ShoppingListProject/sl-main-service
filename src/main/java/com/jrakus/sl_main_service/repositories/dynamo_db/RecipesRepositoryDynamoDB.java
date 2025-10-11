package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.RecipesRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipeMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.Recipe;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import java.util.List;

@Repository
public class RecipesRepositoryDynamoDB implements RecipesRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;
    private final RecipeMapper recipeMapper;

    private final String pkGlobal = "GLOBAL#RECIPES";

    private final String pkPrefix = "USER#";
    private final String skPrefix = "RECIPE#";

    public RecipesRepositoryDynamoDB(
            DynamoDBQueryHelper dynamoDBQueryHelper,
            RecipeMapper recipeMapper) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
        this.recipeMapper = recipeMapper;
    }

    @Override
    public List<Recipe> getAllPublicRecipes() {

        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(this.pkGlobal, this.skPrefix);

        return queryResponse.items()
                .stream()
                .map(recipeMapper::fromDynamoDB)
                .toList();
    }
}
