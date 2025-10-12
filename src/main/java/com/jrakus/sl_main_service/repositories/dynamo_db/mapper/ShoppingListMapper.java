package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.CategorizedItem;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItem;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

@Component
public class ShoppingListMapper {

    // ======================
    // ======== READ ========
    // ======================

    public ShoppingList fromDynamoDBItem(Map<String, AttributeValue> item) {
        ShoppingList shoppingList = new ShoppingList();

        shoppingList.setName(item.get("name").s());
        shoppingList.setCreatedAt(item.get("createdAt").s());
        shoppingList.setUpdatedAt(item.get("updatedAt").s());
        shoppingList.setItemsPerCategory(mapToItemsPerCategory(item.get("itemsPerCategory").l()));

        return shoppingList;
    }

    private List<CategorizedItem> mapToItemsPerCategory(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToCategorizedItems)
                .toList();
    }

    private CategorizedItem mapToCategorizedItems(Map<String, AttributeValue> item) {
        return new CategorizedItem()
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
                .quantity(Integer.valueOf(item.get("quantity").n()))
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
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "name", AttributeValue.builder().s(shoppingList.getName()).build(),
                "createdAt", AttributeValue.builder().s(shoppingList.getCreatedAt()).build(),
                "updatedAt", AttributeValue.builder().s(shoppingList.getUpdatedAt()).build(),
                "itemsPerCategory", AttributeValue.builder()
                        .l(mapFromItemsPerCategory(shoppingList.getItemsPerCategory()))
                        .build()
        );
    }

    private List<AttributeValue> mapFromItemsPerCategory(List<CategorizedItem> itemsPerCategory) {
        return itemsPerCategory.stream()
                .map(this::mapFromCategorizedItem)
                .toList();
    }

    private AttributeValue mapFromCategorizedItem(CategorizedItem categorizedItem) {
        return AttributeValue.builder()
                .m(Map.of(
                        "category", AttributeValue.builder().s(categorizedItem.getCategory()).build(),
                        "items", AttributeValue.builder()
                                .l(mapFromShoppingListItems(categorizedItem.getItems()))
                                .build()
                ))
                .build();
    }

    private List<AttributeValue> mapFromShoppingListItems(List<ShoppingListItem> items) {
        return items.stream()
                .map(this::mapFromShoppingListItem)
                .toList();
    }

    private AttributeValue mapFromShoppingListItem(ShoppingListItem item) {
        return AttributeValue.builder()
                .m(Map.of(
                        "name", AttributeValue.builder().s(item.getName()).build(),
                        "quantity", AttributeValue.builder().n(String.valueOf(item.getQuantity())).build(),
                        "unit", AttributeValue.builder().s(item.getUnit()).build(),
                        "purchased", AttributeValue.builder().bool(item.getPurchased()).build()
                ))
                .build();
    }
}
