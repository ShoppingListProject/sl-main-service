package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.MetadataRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipesMetadataMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListsMetadataMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.RecipeInfo;
import org.openapitools.model.ShoppingListInfo;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class MetadataRepositoryDynamoDB implements MetadataRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;

    private final ShoppingListsMetadataMapper shoppingListMetadataMapper;
    private final RecipesMetadataMapper recipesMetadataMapper;

    private final String pkPrefix = "USER#";
    private final String skShoppingList = "METADATA#SHOPPING_LISTS";
    private final String skRecipe = "METADATA#RECIPES";

    public MetadataRepositoryDynamoDB(
            DynamoDBQueryHelper dynamoDBQueryHelper,
            ShoppingListsMetadataMapper shoppingListMetadataMapper,
            RecipesMetadataMapper recipesMetadataMapper
    ) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
        this.shoppingListMetadataMapper = shoppingListMetadataMapper;
        this.recipesMetadataMapper = recipesMetadataMapper;
    }

    @Override
    public List<ShoppingListInfo> getShoppingListMetadata(String userId) {

        String pk = pkPrefix + userId;
        Map<String, AttributeValue> responseItem = dynamoDBQueryHelper.getSingleItem(pk, skShoppingList);

        // Handle case when user creates his first data and there is no metadata yet.
        if (responseItem.isEmpty()) {
            responseItem = shoppingListMetadataMapper.toDynamoDBItem(pk, skShoppingList, new ArrayList<>());
        }

        return shoppingListMetadataMapper.fromDynamoDBItem(responseItem);
    }

    @Override
    public List<RecipeInfo> getRecipeMetadata(String userId) {

        String pk = pkPrefix + userId;
        Map<String, AttributeValue> responseItem = dynamoDBQueryHelper.getSingleItem(pk, skRecipe);

        // Handle case when user creates his first data and there is no metadata yet.
        if (responseItem.isEmpty()) {
            responseItem = recipesMetadataMapper.toDynamoDBItem(pk, skRecipe, new ArrayList<>());
        }

        return recipesMetadataMapper.fromDynamoDBItem(responseItem);
    }

    @Override
    public void saveShoppingListMetadata(String userId, List<ShoppingListInfo> shoppingListMetadataList) {
        String pk = pkPrefix + userId;

        Map<String, AttributeValue> dynamoDBItem = shoppingListMetadataMapper.toDynamoDBItem(
                pk,
                skShoppingList,
                shoppingListMetadataList
        );

        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }

    @Override
    public void saveRecipeMetadata(String userId, List<RecipeInfo> recipeMetadataList) {
        String pk = pkPrefix + userId;

        Map<String, AttributeValue> dynamoDBItem = recipesMetadataMapper.toDynamoDBItem(
                pk,
                skRecipe,
                recipeMetadataList
        );

        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }
}
