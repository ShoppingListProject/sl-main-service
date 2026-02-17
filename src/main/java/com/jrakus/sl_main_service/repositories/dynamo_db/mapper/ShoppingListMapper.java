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

        OffsetDateTime updatedAt = OffsetDateTime.parse(skArray[1]);
        String shoppingListId = skArray[2];

        shoppingList.setShoppingListId(shoppingListId);
        shoppingList.setName(item.get("name").s());
        shoppingList.setCreatedAt(OffsetDateTime.parse(item.get("createdAt").s()));
        shoppingList.setUpdatedAt(updatedAt);
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
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "name", AttributeValue.builder().s(shoppingList.getName()).build(),
                "createdAt", AttributeValue.builder().s(shoppingList.getCreatedAt().toString()).build(),
                "itemsPerCategory", AttributeValue.builder()
                        .l(mapFromItemsPerCategory(shoppingList.getItemsPerCategory()))
                        .build()
        );
    }

    private List<AttributeValue> mapFromItemsPerCategory(List<CategorizedItems> itemsPerCategory) {
        return itemsPerCategory.stream()
                .map(this::mapFromCategorizedItem)
                .toList();
    }

    private AttributeValue mapFromCategorizedItem(CategorizedItems categorizedItem) {
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
