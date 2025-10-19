package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipeMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeBase;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class RecipeRepositoryDynamoDB implements RecipeRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;
    private final RecipeMapper recipeMapper;

    private final String pkGlobal = "GLOBAL#RECIPES";

    private final String pkPrefix = "USER#";
    private final String skPrefix = "RECIPE#";

    public RecipeRepositoryDynamoDB(
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

    @Override
    public List<Recipe> getSpecificPublicRecipes(List<String> recipeIds) {

        if(recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> listOfSk = new ArrayList<>();

        for(String recipeId: recipeIds) {
            String sk = this.skPrefix + recipeId;
            listOfSk.add(sk);
        }

        List<Map<String, AttributeValue>> queryResponse = dynamoDBQueryHelper.getManyItems(pkGlobal, listOfSk);

        return queryResponse.stream()
                .map(recipeMapper::fromDynamoDB)
                .toList();
    }

    @Override
    public List<Recipe> getRecipesForUser(String userId) {

        String pk = pkPrefix + userId;

        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(pk, this.skPrefix);

        return queryResponse.items()
                .stream()
                .map(recipeMapper::fromDynamoDB)
                .toList();
    }

    @Override
    public List<Recipe> getSpecificRecipesForUser(String userId, List<String> recipeIds) {

        if(recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        String pk = pkPrefix + userId;
        List<String> listOfSk = new ArrayList<>();

        for(String recipeId: recipeIds) {
            String sk = this.skPrefix + recipeId;
            listOfSk.add(sk);
        }

        List<Map<String, AttributeValue>> queryResponse = dynamoDBQueryHelper.getManyItems(pk, listOfSk);

        return queryResponse.stream()
                .map(recipeMapper::fromDynamoDB)
                .toList();
    }

    @Override
    public void saveRecipeForUser(String userId, Recipe recipe) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + recipe.getRecipeId();

        RecipeBase recipeBase = new RecipeBase()
                .name(recipe.getName())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .items(recipe.getItems());

        Map<String, AttributeValue> dynamoDBItem = recipeMapper.toDynamoDBItem(pk, sk, recipeBase);
        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }

    @Override
    public void deleteRecipeForUser(String userId, String recipeId) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + recipeId;

        dynamoDBQueryHelper.deleteSingleItem(pk, sk);
    }
}
