package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeMapperUnitTests {

    private static final String PK_GLOBAL = "GLOBAL#RECIPES";
    private static final String PK = "USER#1234";
    private static final String SK= "RECIPE#1234";
    private static final String RECIPE_NAME = "myFirstRecipe";
    private static final String CREATION_TIME = "2025-09-27T15:16:05.951250500Z";
    private static final String UPDATING_TIME = "2026-11-02T12:06:15.951250500Z";

    private final RecipeMapper recipeMapper = new RecipeMapper();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldMapRecipeWithoutItemsFromDynamoDB(boolean isPublic) {

        // given
        Map<String, AttributeValue> emptyRecipeMap = createSimpleRecipeMap(isPublic);

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(emptyRecipeMap, isPublic);

        // then
        checkBasicFields(recipe, isPublic);
        assertEquals(List.of(), recipe.getItems());
    }

    @Test
    void shouldMapRecipeWithOneItemFromDynamoDB() {

        // given
        Map<String, AttributeValue> simpleRecipeMap = createSimpleRecipeMap(true);

        List<AttributeValue> listOfItems = List.of(
                createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables")
        );

        simpleRecipeMap.put("items", AttributeValue.fromL(listOfItems));

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(simpleRecipeMap, false);

        // then
        checkBasicFields(recipe, false);
        assertEquals(1, recipe.getItems().size());

        RecipeItem expectedRecipeItem = new RecipeItem("cucumber", 1f, "kg", "vegetables");
        assertEquals(expectedRecipeItem, recipe.getItems().getFirst());
    }

    @Test
    void shouldMapRecipeWithMultipleItemsFromDynamoDB() {

        // given
        Map<String, AttributeValue> simpleRecipeMap = createSimpleRecipeMap(true);

        List<AttributeValue> listOfItems = List.of(
                createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables"),
                createRecipeItemAsMap("carrot", 0.5f, "kg", "vegetables"),
                createRecipeItemAsMap("minced pork", 700f, "g", "meat"),
                createRecipeItemAsMap("water", 3f, "l", "beverages")
        );

        simpleRecipeMap.put("items", AttributeValue.fromL(listOfItems));

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(simpleRecipeMap, false);

        // then
        checkBasicFields(recipe, false);
        assertEquals(4, recipe.getItems().size());

        List<RecipeItem> expectedRecipeItems = List.of(
                new RecipeItem("cucumber", 1f, "kg", "vegetables"),
                new RecipeItem("carrot", 0.5f, "kg", "vegetables"),
                new RecipeItem("minced pork", 700f, "g", "meat"),
                new RecipeItem("water", 3f, "l", "beverages")
        );

        assertEquals(expectedRecipeItems, recipe.getItems());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldMapRecipeWithoutItemsToDynamoDB(boolean isPublic) {

        // given
        Recipe recipe = createSimpleRecipe(isPublic);

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(isPublic ? PK_GLOBAL : PK, SK, recipe);

        // then
        checkBasicFields(recipeAsMap, isPublic);
        assertEquals(List.of(), recipeAsMap.get("items").l());
    }

    @Test
    void shouldMapRecipeWithOneItemToDynamoDB() {

        // given
        Recipe recipe = createSimpleRecipe(false);
        RecipeItem recipeItem = new RecipeItem("cucumber", 1f, "kg", "vegetables");
        recipe.setItems(List.of(recipeItem));

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(PK, SK, recipe);

        // then
        checkBasicFields(recipeAsMap, false);

        AttributeValue expectedRecipeItem = createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables");
        List<AttributeValue> actualRecipeItems = recipeAsMap.get("items").l();

        assertEquals(1, actualRecipeItems.size());
        assertEquals(expectedRecipeItem, actualRecipeItems.getFirst());
    }

    @Test
    void shouldMapRecipeWithMultipleItemsToDynamoDB() {

        // given
        Recipe recipe = createSimpleRecipe(false);
        List<RecipeItem> LisOfRecipeItems = List.of(
                new RecipeItem("cucumber", 1f, "kg", "vegetables"),
                new RecipeItem("carrot", 0.5f, "kg", "vegetables"),
                new RecipeItem("minced pork", 700f, "g", "meat"),
                new RecipeItem("water", 3f, "l", "beverages")
        );
        recipe.setItems(LisOfRecipeItems);

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(PK, SK, recipe);

        // then
        checkBasicFields(recipeAsMap, false);

        List<AttributeValue> expectedListOfRecipeItems = List.of(
                createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables"),
                createRecipeItemAsMap("carrot", 0.5f, "kg", "vegetables"),
                createRecipeItemAsMap("minced pork", 700f, "g", "meat"),
                createRecipeItemAsMap("water", 3f, "l", "beverages")
        );

        List<AttributeValue> actualRecipeItems = recipeAsMap.get("items").l();

        assertEquals(expectedListOfRecipeItems, actualRecipeItems);
    }


    private Map<String, AttributeValue> createSimpleRecipeMap(boolean isRecipePublic) {
        return new HashMap<>(
                Map.of(
                    "PK", AttributeValue.fromS(isRecipePublic ? PK_GLOBAL : PK),
                    "SK", AttributeValue.fromS(SK),
                    "name", AttributeValue.fromS(RECIPE_NAME),
                    "createdAt", AttributeValue.fromS(CREATION_TIME),
                    "updatedAt", AttributeValue.fromS(UPDATING_TIME),
                    "items", AttributeValue.fromL(List.of())
                )
        );
    }

    private AttributeValue createRecipeItemAsMap(String name, Float quantity, String unit, String category) {
        return AttributeValue.fromM(
                Map.of(
                        "name", AttributeValue.fromS(name),
                        "quantity", AttributeValue.fromN(String.valueOf(quantity)),
                        "unit", AttributeValue.fromS(unit),
                        "category", AttributeValue.fromS(category)
                )
        );
    }

    private void checkBasicFields(Recipe recipe, boolean isPublicRecipe) {
        assertEquals("1234", recipe.getRecipeId());
        assertEquals(RECIPE_NAME, recipe.getName());
        assertEquals(isPublicRecipe, recipe.getIsGlobal());
        assertEquals(OffsetDateTime.parse(CREATION_TIME), recipe.getCreatedAt());
        assertEquals(OffsetDateTime.parse(UPDATING_TIME), recipe.getUpdatedAt());
    }

    private Recipe createSimpleRecipe(boolean isRecipePublic) {
        return new Recipe(
                RECIPE_NAME,
                List.of(),
                isRecipePublic,
                "1234",
                OffsetDateTime.parse(UPDATING_TIME),
                OffsetDateTime.parse(CREATION_TIME)
        );
    }

    private void checkBasicFields(Map<String, AttributeValue> recipeAsMap, boolean isPublicRecipe) {
        assertEquals(isPublicRecipe ? PK_GLOBAL : PK, recipeAsMap.get("PK").s());
        assertEquals(SK, recipeAsMap.get("SK").s());
        assertEquals(RECIPE_NAME, recipeAsMap.get("name").s());
        assertEquals(CREATION_TIME, recipeAsMap.get("createdAt").s());
        assertEquals(UPDATING_TIME, recipeAsMap.get("updatedAt").s());
    }

}
