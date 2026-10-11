package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.ShoppingList;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
    public List<ShoppingList> getShoppingListsForUser(String userId, List<String> shoppingListIds) {

        String pk = this.pkPrefix + userId;
        List<String> listOfSk = new ArrayList<>();

        for(String shoppingListId: shoppingListIds) {
            String sk = this.skPrefix + shoppingListId;
            listOfSk.add(sk);
        }

        var responseItems = dynamoDBQueryHelper.getManyItems(
                pk,
                listOfSk
        );

        // BatchGetItem does not guarantee the order of returned items, so restore the requested one
        return responseItems
                .stream()
                .map(shoppingListMapper::fromDynamoDBItem)
                .sorted(Comparator.comparingInt(list -> shoppingListIds.indexOf(list.getShoppingListId())))
                .toList();
    }

    @Override
    public Optional<ShoppingList> getUserShoppingListById(String userId, String shoppingListId) {
        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingListId;

        var responseItem = dynamoDBQueryHelper.getSingleItem(pk, sk);

        if(responseItem.isEmpty())
            return Optional.empty();

        return Optional.of(shoppingListMapper.fromDynamoDBItem(responseItem));
    }

    @Override
    public void saveShoppingListForUser(String userId, ShoppingList shoppingList) {

        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingList.getShoppingListId();

        var dynamoDBItem = shoppingListMapper.toDynamoDBItem(pk, sk, shoppingList);
        dynamoDBQueryHelper.saveSingleItem(dynamoDBItem);
    }

    @Override
    public void deleteShoppingListForUser(String userId, String shoppingListId) {
        String pk = this.pkPrefix + userId;
        String sk = this.skPrefix + shoppingListId;

        dynamoDBQueryHelper.deleteSingleItem(pk, sk);
    }
}
