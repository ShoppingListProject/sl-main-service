package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipeMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecipeRepositoryDynamoDBIntegrationTests extends DynamoDBIntegrationTestBase {

    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";

    private final RecipeRepositoryDynamoDB recipeRepository =
            new RecipeRepositoryDynamoDB(dynamoDBQueryHelper, new RecipeMapper());

    @Test
    void shouldSaveAndReadRecipeForUser() {

        // given
        Recipe recipe = createRecipe("1", "pancakes");

        // when
        recipeRepository.saveRecipeForUser(USER_ID, recipe);

        // then
        Optional<Recipe> result = recipeRepository.getUserRecipeById(USER_ID, "1");
        assertEquals(Optional.of(recipe), result);
    }

    @Test
    void shouldReturnEmptyWhenRecipeDoesNotExist() {

        // when
        Optional<Recipe> result = recipeRepository.getUserRecipeById(USER_ID, "missing");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyRecipesOfGivenUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes"));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("2", "soup"));
        recipeRepository.saveRecipeForUser(OTHER_USER_ID, createRecipe("3", "salad"));

        // when
        List<Recipe> result = recipeRepository.getRecipesForUser(USER_ID);

        // then
        assertEquals(List.of("1", "2"), result.stream().map(Recipe::getRecipeId).sorted().toList());
    }

    @Test
    void shouldDeleteRecipeForUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes"));

        // when
        recipeRepository.deleteRecipeForUser(USER_ID, "1");

        // then
        assertTrue(recipeRepository.getUserRecipeById(USER_ID, "1").isEmpty());
    }

    private Recipe createRecipe(String recipeId, String name) {
        return new Recipe()
                .recipeId(recipeId)
                .name(name)
                .createdAt(OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z"))
                .updatedAt(OffsetDateTime.parse("2026-11-02T12:06:15.951250500Z"))
                .items(List.of(new RecipeItem("flour", 500f, "g", "Pantry / Dry Goods")))
                .isGlobal(false);
    }
}
