package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.openapitools.model.CategorizedItem;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItem;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ShoppingListRepositoryDynamoDB implements ShoppingListRepository {

    private final DynamoDBQueryHelper dynamoDBQueryHelper;

    private final String pkPrefix = "USER#";
    private final String skPrefix = "SHOPPING_LIST#";

    public ShoppingListRepositoryDynamoDB(DynamoDBQueryHelper dynamoDBQueryHelper) {
        this.dynamoDBQueryHelper = dynamoDBQueryHelper;
    }

    public List<ShoppingList> getAllShoppingListsForUser(String userId) {

        String pk = this.pkPrefix + userId;
        QueryResponse queryResponse = dynamoDBQueryHelper.queryUsingPKAndSKPrefix(pk, this.skPrefix);

        return queryResponse.items().stream()
                .map(shoppingListDynamoDB -> {

                    ShoppingList shoppingList = new ShoppingList();

                    shoppingList.setName(shoppingListDynamoDB.get("name").s());
                    shoppingList.setCreatedAt(shoppingListDynamoDB.get("createdAt").s());
                    shoppingList.setUpdatedAt(shoppingListDynamoDB.get("updatedAt").s());

                    List<CategorizedItem> itemsPerCategory = shoppingListDynamoDB.get("itemsPerCategory")
                            .l()
                            .stream()
                            .map(itemsPerCategoryDynamoDB -> {

                                CategorizedItem categorizedItem = new CategorizedItem();

                                 String category = itemsPerCategoryDynamoDB.m().get("category").s();

                                 List<ShoppingListItem> shoppingListItemList =  itemsPerCategoryDynamoDB.m()
                                         .get("items")
                                         .l()
                                         .stream()
                                         .map(item -> {

                                     ShoppingListItem shoppingListItem = new ShoppingListItem();

                                     String name = item.m().get("name").s();
                                     Integer quantity = Integer.valueOf(item.m().get("quantity").n());
                                     String unit = item.m().get("unit").s();
                                     Boolean purchased = item.m().get("purchased").bool();

                                     shoppingListItem.setName(name);
                                     shoppingListItem.setQuantity(quantity);
                                     shoppingListItem.setUnit(unit);
                                     shoppingListItem.setPurchased(purchased);

                                     return shoppingListItem;
                                 }).toList();

                                categorizedItem.setCategory(category);
                                categorizedItem.setItems(shoppingListItemList);

                                return categorizedItem;
                            }).toList();

                    return shoppingList;

                }).toList();
    }

}
