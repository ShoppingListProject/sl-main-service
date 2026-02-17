package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.MetadataRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListsMetadataMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.ShoppingListInfo;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

@Repository
public class MetadataRepositoryDynamoDB implements MetadataRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;
    private final ShoppingListsMetadataMapper shoppingListMetadataMapper;

    private final String pkPrefix = "USER#";
    private final String skShoppingList = "METADATA#SHOPPING_LISTS";

    public MetadataRepositoryDynamoDB(
            DynamoDBQueryHelper dynamoDBQueryHelper,
            ShoppingListsMetadataMapper shoppingListMetadataMapper
    ) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
        this.shoppingListMetadataMapper = shoppingListMetadataMapper;
    }

    @Override
    public List<ShoppingListInfo> getShoppingListMetadata(String userId) {

        String pk = pkPrefix + userId;
        Map<String, AttributeValue> responseItem = dynamoDBQueryHelper.getSingleItem(pk, skShoppingList);

        return shoppingListMetadataMapper.fromDynamoDBItem(responseItem);
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
}
