package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.ShoppingList;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import java.util.List;
import java.util.Map;

@Repository
public class ShoppingListRepositoryDynamoDB implements ShoppingListRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;
    private final ShoppingListMapper shoppingListMapper;

    private final String pkPrefix = "USER#";
    private final String skPrefix = "SHOPPING_LIST#";

    public ShoppingListRepositoryDynamoDB(
            DynamoDBQueryHelper dynamoDBQueryHelper,
            ShoppingListMapper shoppingListMapper) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
        this.shoppingListMapper = shoppingListMapper;
    }

    public List<ShoppingList> getShoppingListsForUser(String userId) {

        String pk = this.pkPrefix + userId;
        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(pk, this.skPrefix);

        return queryResponse.items()
                .stream()
                .map(shoppingListMapper::fromDynamoDBItem)
                .toList();
    }

    @Override
    public void saveShoppingListForUser(String userId, ShoppingList shoppingList) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingList.getShoppingListId();

        Map<String, AttributeValue> dynamoDBItem = shoppingListMapper.toDynamoDBItem(pk, sk, shoppingList);
        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }

    @Override
    public void deleteShoppingListForUser(String userId, String shoppingListId) {
        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingListId;

        dynamoDBQueryHelper.deleteSingleItem(pk, sk);
    }
}
