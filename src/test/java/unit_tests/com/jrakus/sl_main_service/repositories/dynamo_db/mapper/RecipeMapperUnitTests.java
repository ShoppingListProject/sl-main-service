package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.api.Test;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeMapperUnitTests {

    private static final String pkGlobal = "GLOBAL#RECIPES";
    private static final String pk = "USER#1234";
    private static final String sk= "RECIPE#1234";
    private static final String recipeName = "myFirstRecipe";
    private static final String creationTime = "2025-09-27T15:16:05.951250500Z";
    private static final String updatingTime = "2026-11-02T12:06:15.951250500Z";

    private final RecipeMapper recipeMapper = new RecipeMapper();

    @Test
    void testMappingFromDynamoDBForTrivialRecipe() {

        // given
        Map<String, AttributeValue> emptyRecipeMap = createSimpleRecipeMapWithoutItems(false);

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(emptyRecipeMap, false);

        // then
        checkBasicFields(recipe, false);
        assertEquals(List.of(), recipe.getItems());
    }

    @Test
    void testMappingFromDynamoDBForPublicTrivialRecipe() {

        // given
        Map<String, AttributeValue> emptyRecipeMap = createSimpleRecipeMapWithoutItems(true);

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(emptyRecipeMap, true);

        // then
        checkBasicFields(recipe, true);
        assertEquals(List.of(), recipe.getItems());
    }

    @Test
    void testMappingFromDynamoDBForSimpleRecipe() {

        // given
        Map<String, AttributeValue> simpleRecipeMap = createSimpleRecipeMapWithoutItems(true);

        List<AttributeValue> listOfItems = List.of(
                createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables")
        );

        simpleRecipeMap.put("items", AttributeValue.builder().l(
                listOfItems
        ).build());

        // when
        Recipe recipe = recipeMapper.fromDynamoDB(simpleRecipeMap, false);

        // then
        checkBasicFields(recipe, false);
        assertEquals(1, recipe.getItems().size());

        RecipeItem expectedRecipeItem = new RecipeItem("cucumber", 1f, "kg", "vegetables");
        assertEquals(expectedRecipeItem, recipe.getItems().getFirst());
    }

    @Test
    void testMappingFromDynamoDBForLargeRecipe() {

        // given
        Map<String, AttributeValue> simpleRecipeMap = createSimpleRecipeMapWithoutItems(true);

        List<AttributeValue> listOfItems = List.of(
                createRecipeItemAsMap("cucumber", 1f, "kg", "vegetables"),
                createRecipeItemAsMap("carrot", 0.5f, "kg", "vegetables"),
                createRecipeItemAsMap("minced pork", 700f, "g", "meat"),
                createRecipeItemAsMap("water", 3f, "l", "beverages")
        );

        simpleRecipeMap.put("items", AttributeValue.builder().l(
                listOfItems
        ).build());

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

    @Test
    void testMappingToDynamoDBForTrivialRecipe() {

        // given
        Recipe recipe = createSimpleRecipeWithoutItems(false);

        // when
        Map<String, AttributeValue> recipeAsMap = recipeMapper.toDynamoDBItem(pk, sk, recipe);

        // then
        checkBasicFields(recipeAsMap, false);
        assertEquals(List.of(), recipeAsMap.get("items").l());
    }


    private Map<String, AttributeValue> createSimpleRecipeMapWithoutItems(boolean isRecipePublic) {
        return new HashMap<>(
                Map.of(
                    "PK", AttributeValue.builder().s(isRecipePublic ? pkGlobal : pk).build(),
                    "SK", AttributeValue.builder().s(sk).build(),
                    "name", AttributeValue.builder().s(recipeName).build(),
                    "createdAt", AttributeValue.builder().s(creationTime).build(),
                    "updatedAt", AttributeValue.builder().s(updatingTime).build(),
                    "items", AttributeValue.builder().l(List.of()).build()
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
        assertEquals(recipeName, recipe.getName());
        assertEquals(isPublicRecipe, recipe.getIsGlobal());
        assertEquals(OffsetDateTime.parse(creationTime), recipe.getCreatedAt());
        assertEquals(OffsetDateTime.parse(updatingTime), recipe.getUpdatedAt());
    }

    private Recipe createSimpleRecipeWithoutItems(boolean isRecipePublic) {
        return new Recipe(
                recipeName,
                List.of(),
                isRecipePublic,
                "1234",
                OffsetDateTime.parse(updatingTime),
                OffsetDateTime.parse(creationTime)
        );
    }

    private void checkBasicFields(Map<String, AttributeValue> recipeAsMap, boolean isPublicRecipe) {
        assertEquals(isPublicRecipe ? pkGlobal : pk, recipeAsMap.get("PK").s());
        assertEquals(sk, recipeAsMap.get("SK").s());
        assertEquals(recipeName, recipeAsMap.get("name").s());
        assertEquals(creationTime, recipeAsMap.get("createdAt").s());
        assertEquals(updatingTime, recipeAsMap.get("updatedAt").s());
    }

}
