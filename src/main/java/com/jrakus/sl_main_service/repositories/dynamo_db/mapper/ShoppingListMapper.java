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

    public ShoppingList fromDynamoDBItem(Map<String, AttributeValue> item) {
        ShoppingList shoppingList = new ShoppingList();

        shoppingList.setName(item.get("name").s());
        shoppingList.setCreatedAt(item.get("createdAt").s());
        shoppingList.setUpdatedAt(item.get("updatedAt").s());
        shoppingList.setItemsPerCategory(mapItemsPerCategory(item.get("itemsPerCategory").l()));

        return shoppingList;
    }

    private List<CategorizedItem> mapItemsPerCategory(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapCategorizedItems)
                .toList();
    }

    private CategorizedItem mapCategorizedItems(Map<String, AttributeValue> item) {
        return new CategorizedItem()
                .category(item.get("category").s())
                .items(mapShoppingListItems(item.get("items").l()));
    }

    private List<ShoppingListItem> mapShoppingListItems(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapShoppingListItem)
                .toList();
    }

    private ShoppingListItem mapShoppingListItem(Map<String, AttributeValue> item) {
        return new ShoppingListItem()
                .name(item.get("name").s())
                .unit(item.get("unit").s())
                .quantity(Integer.valueOf(item.get("quantity").n()))
                .purchased(item.get("purchased").bool());
    }
}
