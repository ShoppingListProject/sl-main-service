package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.model.CategorizedItems;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItem;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingListMapperUnitTests {

    private static final String PK = "USER#1234";
    private static final String SHOPPING_LIST_ID = "5678";
    private static final String SHOPPING_LIST_NAME = "weekend shopping";
    private static final String CREATION_TIME = "2025-09-27T15:16:05.951250500Z";
    private static final String UPDATING_TIME = "2026-11-02T12:06:15.951250500Z";
    private static final String SK = "SHOPPING_LIST#" + SHOPPING_LIST_ID;

    private static final CategorizedItems VEGETABLES = new CategorizedItems(
            "vegetables",
            List.of(
                    new ShoppingListItem("cucumber", 1f, "kg", false),
                    new ShoppingListItem("carrot", 0.1f, "kg", true)
            )
    );
    private static final CategorizedItems MEAT = new CategorizedItems(
            "meat",
            List.of(new ShoppingListItem("minced pork", 700f, "g", false))
    );
    private static final CategorizedItems EMPTY_CATEGORY = new CategorizedItems("beverages", List.of());

    private final ShoppingListMapper shoppingListMapper = new ShoppingListMapper();

    static Stream<List<CategorizedItems>> itemsPerCategoryLists() {
        return Stream.of(
                List.of(),
                List.of(EMPTY_CATEGORY),
                List.of(MEAT),
                List.of(VEGETABLES, MEAT, EMPTY_CATEGORY)
        );
    }

    @ParameterizedTest
    @MethodSource("itemsPerCategoryLists")
    void shouldMapShoppingListFromDynamoDB(List<CategorizedItems> itemsPerCategory) {

        // given
        Map<String, AttributeValue> shoppingListMap = createShoppingListMap(itemsPerCategory);

        // when
        ShoppingList shoppingList = shoppingListMapper.fromDynamoDBItem(shoppingListMap);

        // then
        assertEquals(createShoppingList(itemsPerCategory), shoppingList);
    }

    @ParameterizedTest
    @MethodSource("itemsPerCategoryLists")
    void shouldMapShoppingListToDynamoDB(List<CategorizedItems> itemsPerCategory) {

        // given
        ShoppingList shoppingList = createShoppingList(itemsPerCategory);

        // when
        Map<String, AttributeValue> shoppingListAsMap = shoppingListMapper.toDynamoDBItem(PK, SK, shoppingList);

        // then
        assertEquals(createShoppingListMap(itemsPerCategory), shoppingListAsMap);
    }

    @Test
    void shouldKeepShoppingListUnchangedAfterRoundTrip() {

        // given
        ShoppingList shoppingList = createShoppingList(List.of(VEGETABLES, MEAT, EMPTY_CATEGORY));

        // when
        Map<String, AttributeValue> shoppingListAsMap = shoppingListMapper.toDynamoDBItem(PK, SK, shoppingList);
        ShoppingList result = shoppingListMapper.fromDynamoDBItem(shoppingListAsMap);

        // then
        assertEquals(shoppingList, result);
    }

    private Map<String, AttributeValue> createShoppingListMap(List<CategorizedItems> itemsPerCategory) {
        return Map.of(
                "PK", AttributeValue.fromS(PK),
                "SK", AttributeValue.fromS(SK),
                "name", AttributeValue.fromS(SHOPPING_LIST_NAME),
                "createdAt", AttributeValue.fromS(CREATION_TIME),
                "updatedAt", AttributeValue.fromS(UPDATING_TIME),
                "itemsPerCategory", AttributeValue.fromL(
                        itemsPerCategory.stream().map(this::createCategorizedItemsAsMap).toList()
                )
        );
    }

    private AttributeValue createCategorizedItemsAsMap(CategorizedItems categorizedItems) {
        return AttributeValue.fromM(Map.of(
                "category", AttributeValue.fromS(categorizedItems.getCategory()),
                "items", AttributeValue.fromL(
                        categorizedItems.getItems().stream().map(this::createShoppingListItemAsMap).toList()
                )
        ));
    }

    private AttributeValue createShoppingListItemAsMap(ShoppingListItem item) {
        return AttributeValue.fromM(Map.of(
                "name", AttributeValue.fromS(item.getName()),
                "quantity", AttributeValue.fromN(String.valueOf(item.getQuantity())),
                "unit", AttributeValue.fromS(item.getUnit()),
                "purchased", AttributeValue.fromBool(item.getPurchased())
        ));
    }

    private ShoppingList createShoppingList(List<CategorizedItems> itemsPerCategory) {
        return new ShoppingList(
                SHOPPING_LIST_NAME,
                itemsPerCategory,
                SHOPPING_LIST_ID,
                OffsetDateTime.parse(UPDATING_TIME),
                OffsetDateTime.parse(CREATION_TIME)
        );
    }
}
