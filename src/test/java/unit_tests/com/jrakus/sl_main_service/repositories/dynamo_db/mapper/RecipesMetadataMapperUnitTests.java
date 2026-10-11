package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.model.RecipeInfo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecipesMetadataMapperUnitTests {

    private static final String PK = "USER#1234";
    private static final String SK = "METADATA#RECIPES";

    private static final RecipeInfo SPAGHETTI =
            new RecipeInfo("spaghetti", "1234", OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z"));
    private static final RecipeInfo PIZZA =
            new RecipeInfo("pizza", "1235", OffsetDateTime.parse("2025-10-28T15:16:05.951250500Z"));
    private static final RecipeInfo CHICKEN =
            new RecipeInfo("chicken with rice", "1236", OffsetDateTime.parse("2025-10-28T12:16:05.951450500Z"));

    private final RecipesMetadataMapper recipesMetadataMapper = new RecipesMetadataMapper();

    static Stream<Arguments> recipeInfoLists() {
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(SPAGHETTI)),
                Arguments.of(List.of(SPAGHETTI, PIZZA, CHICKEN))
        );
    }

    @ParameterizedTest
    @MethodSource("recipeInfoLists")
    void shouldMapMetadataFromDynamoDB(List<RecipeInfo> recipeInfos) {

        // given
        Map<String, AttributeValue> metadata = createMetadataMap(recipeInfos);

        // when
        List<RecipeInfo> result = recipesMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(recipeInfos, result);
    }

    @ParameterizedTest
    @MethodSource("recipeInfoLists")
    void shouldMapMetadataToDynamoDB(List<RecipeInfo> recipeInfos) {

        // when
        Map<String, AttributeValue> result = recipesMetadataMapper.toDynamoDBItem(PK, SK, recipeInfos);

        // then
        assertEquals(createMetadataMap(recipeInfos), result);
    }

    @ParameterizedTest
    @MethodSource("recipeInfoLists")
    void shouldKeepMetadataUnchangedAfterRoundTrip(List<RecipeInfo> recipeInfos) {

        // when
        Map<String, AttributeValue> metadata = recipesMetadataMapper.toDynamoDBItem(PK, SK, recipeInfos);
        List<RecipeInfo> result = recipesMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(recipeInfos, result);
    }

    private Map<String, AttributeValue> createMetadataMap(List<RecipeInfo> recipeInfos) {
        return Map.of(
                "PK", AttributeValue.fromS(PK),
                "SK", AttributeValue.fromS(SK),
                "metadata", AttributeValue.fromL(recipeInfos.stream().map(this::createRecipeInfoAsMap).toList())
        );
    }

    private AttributeValue createRecipeInfoAsMap(RecipeInfo info) {
        return AttributeValue.fromM(Map.of(
                "recipeName", AttributeValue.fromS(info.getRecipeName()),
                "id", AttributeValue.fromS(info.getId()),
                "updatedAt", AttributeValue.fromS(info.getUpdatedAt().toString())
        ));
    }
}
