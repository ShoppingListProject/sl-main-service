package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.ShoppingList;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import java.util.List;

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

    public List<ShoppingList> getAllShoppingListsForUser(String userId) {

        String pk = this.pkPrefix + userId;
        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(pk, this.skPrefix);

        return queryResponse.items()
                .stream()
                .map(shoppingListMapper::fromDynamoDBItem)
                .toList();
    }

}
