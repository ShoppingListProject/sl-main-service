package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipeMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.Recipe;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        var responseItems = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(this.pkGlobal, this.skPrefix);

        return responseItems
                .stream()
                .map(item -> recipeMapper.fromDynamoDB(item, true))
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

        var responseItems = dynamoDBQueryHelper.getManyItems(pkGlobal, listOfSk);

        return responseItems.stream()
                .map(item -> recipeMapper.fromDynamoDB(item, true))
                .toList();
    }

    @Override
    public List<Recipe> getRecipesForUser(String userId) {

        String pk = pkPrefix + userId;

        var responseItems = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(pk, this.skPrefix);

        return responseItems
                .stream()
                .map(item -> recipeMapper.fromDynamoDB(item, false))
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

        var responseItems = dynamoDBQueryHelper.getManyItems(pk, listOfSk);

        return responseItems.stream()
                .map(item -> recipeMapper.fromDynamoDB(item, false))
                .toList();
    }

    @Override
    public Optional<Recipe> getUserRecipeById(String userId, String recipeId) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + recipeId;

        var responseItems = dynamoDBQueryHelper.getSingleItem(pk, sk);

        if(responseItems.isEmpty())
            return Optional.empty();

        return Optional.of(recipeMapper.fromDynamoDB(responseItems, false));
    }

    @Override
    public void saveRecipeForUser(String userId, Recipe recipe) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + recipe.getRecipeId();

        var dynamoDBItem = recipeMapper.toDynamoDBItem(pk, sk, recipe);
        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }

    @Override
    public void deleteRecipeForUser(String userId, String recipeId) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + recipeId;

        dynamoDBQueryHelper.deleteSingleItem(pk, sk);
    }
}
