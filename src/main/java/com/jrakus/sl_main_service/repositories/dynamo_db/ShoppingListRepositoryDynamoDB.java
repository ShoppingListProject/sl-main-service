package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.ShoppingList;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public List<ShoppingList> getShoppingListsForUser(String userId, OffsetDateTime from, OffsetDateTime to) {

        String pk = this.pkPrefix + userId;
        String skFrom = this.skPrefix + from;

        /*
        We append "$" to the upper bound because DynamoDB compares sort keys
        lexicographically (byte-order string comparison).
        Our actual SK values have additional characters after `skPrefix + to`
        (e.g., a UUID suffix like "...#<uuid>").

        If we do not add "$" character for the upper bound, the item with the SK that has
        the same "updateAt" like upperBound won't be returned.

        "$" is lexicographically greater than "#" (the separator used before the UUID),
        so this ensures all keys that start with `skPrefix + to + "#"`
        are included.
         */
        String skTo = this.skPrefix + to + "$";

        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKBetween(pk, skFrom, skTo);

        return queryResponse.items()
                .stream()
                .map(shoppingListMapper::fromDynamoDBItem)
                .toList();
    }

    @Override
    public Optional<ShoppingList> getUserShoppingListById(String userId, String shoppingListId) {
        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingListId;

        Map<String, AttributeValue> responseItem = dynamoDBQueryHelper.getSingleItem(pk, sk);

        if(responseItem == null)
            return Optional.empty();

        return Optional.of(shoppingListMapper.fromDynamoDBItem(responseItem));
    }

    @Override
    public void saveShoppingListForUser(String userId, ShoppingList shoppingList) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingList.getUpdatedAt() + "#" + shoppingList.getShoppingListId();

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
