package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RecipeMapperUnitTests {

    private static final String PK_GLOBAL = "GLOBAL#RECIPES";
    private static final String PK = "USER#1234";
    private static final String SK = "RECIPE#1234";
    private static final String RECIPE_ID = "1234";
    private static final String RECIPE_NAME = "myFirstRecipe";
    private static final String CREATION_TIME = "2025-09-27T15:16:05.951250500Z";
    private static final String UPDATING_TIME = "2026-11-02T12:06:15.951250500Z";

    private static final RecipeItem CUCUMBER = new RecipeItem("cucumber", 1f, "kg", "vegetables");
    private static final RecipeItem CARROT = new RecipeItem("carrot", 0.1f, "kg", "vegetables");
    private static final RecipeItem PORK = new RecipeItem("minced pork", 700f, "g", "meat");
    private static final RecipeItem WATER = new RecipeItem("water", 3f, "l", "beverages");

    private final RecipeMapper recipeMapper = new RecipeMapper();

    static Stream<Arguments> itemLists() {
        return Stream.of(
                Arguments.of(false, List.of()),
                Arguments.of(true, List.of()),
                Arguments.of(false, List.of(CUCUMBER)),
                Arguments.of(true, List.of(CUCUMBER)),
                Arguments.of(false, List.of(CUCUMBER, CARROT, PORK, WATER)),
                Arguments.of(true, List.of(CUCUMBER, CARROT, PORK, WATER))
        );
    }

    @ParameterizedTest
    @MethodSource("itemLists")
    void shouldMapRecipeFromDynamoDB(boolean isPublic, List<RecipeItem> items) {

        // given
        Map<String, AttributeValue> recipeMap = createRecipeMap(isPublic, items);

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(recipeMap, isPublic);

        // then
        assertEquals(createRecipe(isPublic, items), recipe);
    }

    @ParameterizedTest
    @MethodSource("itemLists")
    void shouldMapRecipeToDynamoDB(boolean isPublic, List<RecipeItem> items) {

        // given
        Recipe recipe = createRecipe(isPublic, items);

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(isPublic ? PK_GLOBAL : PK, SK, recipe);

        // then
        assertEquals(createRecipeMap(isPublic, items), recipeAsMap);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldKeepRecipeUnchangedAfterRoundTrip(boolean isPublic) {

        // given
        Recipe recipe = createRecipe(isPublic, List.of(CUCUMBER, CARROT, PORK, WATER));

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(isPublic ? PK_GLOBAL : PK, SK, recipe);
        Recipe result = recipeMapper.fromDynamoDB(recipeAsMap, isPublic);

        // then
        assertEquals(recipe, result);
    }

    private Map<String, AttributeValue> createRecipeMap(boolean isPublic, List<RecipeItem> items) {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("PK", AttributeValue.fromS(isPublic ? PK_GLOBAL : PK));
        map.put("SK", AttributeValue.fromS(SK));
        map.put("name", AttributeValue.fromS(RECIPE_NAME));
        map.put("createdAt", AttributeValue.fromS(CREATION_TIME));
        map.put("updatedAt", AttributeValue.fromS(UPDATING_TIME));
        map.put("items", AttributeValue.fromL(items.stream().map(this::createRecipeItemAsMap).toList()));
        return map;
    }

    private AttributeValue createRecipeItemAsMap(RecipeItem item) {
        return AttributeValue.fromM(Map.of(
                "name", AttributeValue.fromS(item.getName()),
                "quantity", AttributeValue.fromN(String.valueOf(item.getQuantity())),
                "unit", AttributeValue.fromS(item.getUnit()),
                "category", AttributeValue.fromS(item.getCategory())
        ));
    }

    private Recipe createRecipe(boolean isPublic, List<RecipeItem> items) {
        return new Recipe(
                RECIPE_NAME,
                items,
                isPublic,
                RECIPE_ID,
                OffsetDateTime.parse(UPDATING_TIME),
                OffsetDateTime.parse(CREATION_TIME)
        );
    }
}
