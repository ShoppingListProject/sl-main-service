package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.CategorizedItems;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItem;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ShoppingListMapper {

    // ======================
    // ======== READ ========
    // ======================

    public ShoppingList fromDynamoDBItem(Map<String, AttributeValue> item) {
        ShoppingList shoppingList = new ShoppingList();

        String[] skArray = item.get("SK").s().split("#");

        String shoppingListId = skArray[1];

        shoppingList.setShoppingListId(shoppingListId);
        shoppingList.setName(item.get("name").s());
        shoppingList.setCreatedAt(OffsetDateTime.parse(item.get("createdAt").s()));
        shoppingList.setUpdatedAt(OffsetDateTime.parse(item.get("updatedAt").s()));
        shoppingList.setItemsPerCategory(mapToItemsPerCategory(item.get("itemsPerCategory").l()));

        return shoppingList;
    }

    private List<CategorizedItems> mapToItemsPerCategory(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToCategorizedItems)
                .toList();
    }

    private CategorizedItems mapToCategorizedItems(Map<String, AttributeValue> item) {
        return new CategorizedItems()
                .category(item.get("category").s())
                .items(mapToShoppingListItems(item.get("items").l()));
    }

    private List<ShoppingListItem> mapToShoppingListItems(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToShoppingListItem)
                .toList();
    }


    private ShoppingListItem mapToShoppingListItem(Map<String, AttributeValue> item) {
        return new ShoppingListItem()
                .name(item.get("name").s())
                .unit(item.get("unit").s())
                .quantity(Float.valueOf((item.get("quantity").n())))
                .purchased(item.get("purchased").bool());
    }

    // =======================
    // ======== WRITE ========
    // =======================

    public Map<String, AttributeValue> toDynamoDBItem(
            String pk,
            String sk,
            ShoppingList shoppingList
    ) {
        return Map.of(
                "PK", AttributeValue.fromS(pk),
                "SK", AttributeValue.fromS(sk),
                "name", AttributeValue.fromS(shoppingList.getName()),
                "createdAt", AttributeValue.fromS(shoppingList.getCreatedAt().toString()),
                "updatedAt", AttributeValue.fromS(shoppingList.getUpdatedAt().toString()),
                "itemsPerCategory", AttributeValue.fromL(mapFromItemsPerCategory(shoppingList.getItemsPerCategory()))
        );
    }

    private List<AttributeValue> mapFromItemsPerCategory(List<CategorizedItems> itemsPerCategory) {
        return itemsPerCategory.stream()
                .map(this::mapFromCategorizedItem)
                .toList();
    }

    private AttributeValue mapFromCategorizedItem(CategorizedItems categorizedItem) {
        return AttributeValue.fromM(Map.of(
                "category", AttributeValue.fromS(categorizedItem.getCategory()),
                "items", AttributeValue.fromL(mapFromShoppingListItems(categorizedItem.getItems()))
        ));
    }

    private List<AttributeValue> mapFromShoppingListItems(List<ShoppingListItem> items) {
        return items.stream()
                .map(this::mapFromShoppingListItem)
                .toList();
    }

    private AttributeValue mapFromShoppingListItem(ShoppingListItem item) {
        return AttributeValue.fromM(Map.of(
                "name", AttributeValue.fromS(item.getName()),
                "quantity", AttributeValue.fromN(String.valueOf(item.getQuantity())),
                "unit", AttributeValue.fromS(item.getUnit()),
                "purchased", AttributeValue.fromBool(item.getPurchased())
        ));
    }
}
