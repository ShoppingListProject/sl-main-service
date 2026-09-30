package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.api.Test;
import org.openapitools.model.RecipeInfo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipeMetadataMapperUnitTests {

    private final String PK = "USER#1234";
    private final String SK = "METADATA#RECIPES";

    private final RecipesMetadataMapper recipesMetadataMapper = new RecipesMetadataMapper();

    @Test
    void shouldMapEmptyMetadataFromDynamoDB() {

        // given
        Map<String, AttributeValue> emptyMetadata = createSimpleMetadataMap();

        // when
        List<RecipeInfo> recipeInfoList = recipesMetadataMapper.fromDynamoDBItem(emptyMetadata);

        // then
        assertEquals(0, recipeInfoList.size());
    }

    @Test
    void shouldMapMetadataWithOneRecipeFromDynamoDB() {

        // given
        Map<String, AttributeValue> metadata = createSimpleMetadataMap();

        List<AttributeValue> recipeInfoListAsMap = List.of(
                createSimpleRecipeInfoAsMap("spaghetti", "1234", "2025-09-27T15:16:05.951250500Z")
        );

        metadata.put("metadata", AttributeValue.fromL(recipeInfoListAsMap));

        // when
        List<RecipeInfo> recipeInfoList = recipesMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(1, recipeInfoList.size());

        RecipeInfo expectedRecipeInfo = new RecipeInfo(
                "spaghetti",
                "1234",
                OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z")
        );

        assertEquals(expectedRecipeInfo, recipeInfoList.getFirst());
    }

    @Test
    void shouldMapMetadataWithManyRecipesFromDynamoDB() {

        // given
        Map<String, AttributeValue> metadata = createSimpleMetadataMap();

        List<AttributeValue> recipeInfoListAsMap = List.of(
                createSimpleRecipeInfoAsMap("spaghetti", "1234", "2025-09-27T15:16:05.951250500Z"),
                createSimpleRecipeInfoAsMap("pizza", "1235", "2025-10-28T15:16:05.951250500Z"),
                createSimpleRecipeInfoAsMap("chicken with rice", "1236", "2025-10-28T12:16:05.951450500Z")
        );

        metadata.put("metadata", AttributeValue.fromL(recipeInfoListAsMap));

        // when
        List<RecipeInfo> recipeInfoList = recipesMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(3, recipeInfoList.size());

        RecipeInfo expectedRecipeInfo1 = new RecipeInfo(
                "spaghetti",
                "1234",
                OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z")
        );
        RecipeInfo expectedRecipeInfo2 = new RecipeInfo(
                "pizza",
                "1235",
                OffsetDateTime.parse("2025-10-28T15:16:05.951250500Z")
        );
        RecipeInfo expectedRecipeInfo3 = new RecipeInfo(
                "chicken with rice",
                "1236",
                OffsetDateTime.parse("2025-10-28T12:16:05.951450500Z")
        );

        assertEquals(expectedRecipeInfo1, recipeInfoList.getFirst());
        assertEquals(expectedRecipeInfo2, recipeInfoList.get(1));
        assertEquals(expectedRecipeInfo3, recipeInfoList.get(2));
    }

    @Test
    void shouldMapEmptyMetadataToDynamoDB() {

        // given
        List<RecipeInfo> emptyRecipeInfoList = List.of();

        // when
        Map<String, AttributeValue> metadata = recipesMetadataMapper.toDynamoDBItem(PK, SK, emptyRecipeInfoList);

        // then
        checkBasicFields(metadata);
        assertEquals(0, metadata.get("metadata").l().size());
    }

    @Test
    void shouldMapMetadataWithOneRecipeToDynamoDB() {

        // given
        List<RecipeInfo> recipeInfoList = List.of(
                new RecipeInfo(
                        "spaghetti",
                        "1234",
                        OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z")
                )
        );

        // when
        Map<String, AttributeValue> metadata = recipesMetadataMapper.toDynamoDBItem(PK, SK, recipeInfoList);

        // then
        checkBasicFields(metadata);

        List<AttributeValue> recipeInfoListResult = metadata.get("metadata").l();

        assertEquals(1, recipeInfoList.size());

        checkRecipeInfoAttributeValue(
                recipeInfoListResult.getFirst(),
                "spaghetti",
                "1234",
                "2025-09-27T15:16:05.951250500Z"
        );
    }

    @Test
    void shouldMapMetadataWithManyRecipesToDynamoDB() {

        // given
        List<RecipeInfo> recipeInfoList = List.of(
                new RecipeInfo(
                        "spaghetti",
                        "1234",
                        OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z")
                ),
                new RecipeInfo(
                        "pizza",
                        "1235",
                        OffsetDateTime.parse("2025-10-28T15:16:05.951250500Z")
                ),
                new RecipeInfo(
                        "chicken with rice",
                        "1236",
                        OffsetDateTime.parse("2025-10-28T12:16:05.951450500Z")
                )
        );

        // when
        Map<String, AttributeValue> metadata = recipesMetadataMapper.toDynamoDBItem(PK, SK, recipeInfoList);

        // then
        checkBasicFields(metadata);

        List<AttributeValue> recipeInfoListResult = metadata.get("metadata").l();

        assertEquals(3, recipeInfoList.size());

        checkRecipeInfoAttributeValue(
                recipeInfoListResult.getFirst(),
                "spaghetti",
                "1234",
                "2025-09-27T15:16:05.951250500Z"
        );

        checkRecipeInfoAttributeValue(
                recipeInfoListResult.get(1),
                "pizza",
                "1235",
                "2025-10-28T15:16:05.951250500Z"
        );

        checkRecipeInfoAttributeValue(
                recipeInfoListResult.get(2),
                "chicken with rice",
                "1236",
                "2025-10-28T12:16:05.951450500Z"
        );
    }

    private Map<String, AttributeValue> createSimpleMetadataMap() {
        return new HashMap<>(Map.of(
                "PK", AttributeValue.fromS(PK),
                "SK", AttributeValue.fromS(SK),
                "metadata", AttributeValue.fromL(List.of())
        ));
    }

    private AttributeValue createSimpleRecipeInfoAsMap(String name, String id, String updatedAt) {
        return AttributeValue.fromM(Map.of(
                "recipeName", AttributeValue.fromS(name),
                "id", AttributeValue.fromS(id),
                "updatedAt", AttributeValue.fromS(updatedAt)
        ));
    }

    private void checkBasicFields(Map<String, AttributeValue> metadata) {
        assertEquals(PK, metadata.get("PK").s());
        assertEquals(SK, metadata.get("SK").s());
    }

    private void checkRecipeInfoAttributeValue(
            AttributeValue recipeInfo,
            String expectedRecipeName,
            String expectedId,
            String expectedUpdatedAt
    ) {
        String recipeName = recipeInfo.m().get("recipeName").s();
        String id = recipeInfo.m().get("id").s();
        String updatedAt = recipeInfo.m().get("updatedAt").s();

        assertEquals(expectedRecipeName, recipeName);
        assertEquals(expectedId, id);
        assertEquals(expectedUpdatedAt, updatedAt);
    }
}
