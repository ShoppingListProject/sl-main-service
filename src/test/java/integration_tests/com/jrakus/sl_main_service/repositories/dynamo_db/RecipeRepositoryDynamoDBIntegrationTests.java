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

    private static final String PK_GLOBAL = "GLOBAL#RECIPES";
    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";

    private final RecipeMapper recipeMapper = new RecipeMapper();
    private final RecipeRepositoryDynamoDB recipeRepository =
            new RecipeRepositoryDynamoDB(dynamoDBQueryHelper, recipeMapper);

    // ======================
    // ===== USER READ ======
    // ======================

    @Test
    void shouldSaveAndReadRecipeForUser() {

        // given
        Recipe recipe = createRecipe("1", "pancakes", false);

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
    void shouldNotReturnRecipeOfOtherUserById() {

        // given
        recipeRepository.saveRecipeForUser(OTHER_USER_ID, createRecipe("1", "pancakes", false));

        // when
        Optional<Recipe> result = recipeRepository.getUserRecipeById(USER_ID, "1");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlyRecipesOfGivenUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("2", "soup", false));
        recipeRepository.saveRecipeForUser(OTHER_USER_ID, createRecipe("3", "salad", false));

        // when
        List<Recipe> result = recipeRepository.getRecipesForUser(USER_ID);

        // then
        assertEquals(List.of("1", "2"), recipeIds(result));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRecipes() {

        // when
        List<Recipe> result = recipeRepository.getRecipesForUser(USER_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnPublicRecipesAsUserRecipes() {

        // given
        savePublicRecipe(createRecipe("1", "pancakes", true));

        // when
        List<Recipe> result = recipeRepository.getRecipesForUser(USER_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlySpecificRecipesForUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("2", "soup", false));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("3", "salad", false));
        recipeRepository.saveRecipeForUser(OTHER_USER_ID, createRecipe("4", "steak", false));

        // when
        List<Recipe> result = recipeRepository.getSpecificRecipesForUser(USER_ID, List.of("1", "3", "4"));

        // then
        assertEquals(List.of("1", "3"), recipeIds(result));
    }

    @Test
    void shouldSkipMissingRecipesWhenReadingSpecificRecipesForUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));

        // when
        List<Recipe> result = recipeRepository.getSpecificRecipesForUser(USER_ID, List.of("1", "missing"));

        // then
        assertEquals(List.of("1"), recipeIds(result));
    }

    @Test
    void shouldReturnEmptyListForEmptyIdsOfUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));

        // when
        List<Recipe> result = recipeRepository.getSpecificRecipesForUser(USER_ID, List.of());

        // then
        assertTrue(result.isEmpty());
    }

    // ========================
    // ===== PUBLIC READ ======
    // ========================

    @Test
    void shouldReturnOnlyPublicRecipes() {

        // given
        savePublicRecipe(createRecipe("1", "pancakes", true));
        savePublicRecipe(createRecipe("2", "soup", true));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("3", "salad", false));

        // when
        List<Recipe> result = recipeRepository.getAllPublicRecipes();

        // then
        assertEquals(List.of("1", "2"), recipeIds(result));
        assertTrue(result.stream().allMatch(Recipe::getIsGlobal));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoPublicRecipes() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));

        // when
        List<Recipe> result = recipeRepository.getAllPublicRecipes();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnOnlySpecificPublicRecipes() {

        // given
        savePublicRecipe(createRecipe("1", "pancakes", true));
        savePublicRecipe(createRecipe("2", "soup", true));
        savePublicRecipe(createRecipe("3", "salad", true));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("4", "steak", false));

        // when
        List<Recipe> result = recipeRepository.getSpecificPublicRecipes(List.of("1", "3", "4"));

        // then
        assertEquals(List.of("1", "3"), recipeIds(result));
        assertTrue(result.stream().allMatch(Recipe::getIsGlobal));
    }

    @Test
    void shouldReturnEmptyListForEmptyPublicIds() {

        // given
        savePublicRecipe(createRecipe("1", "pancakes", true));

        // when
        List<Recipe> result = recipeRepository.getSpecificPublicRecipes(List.of());

        // then
        assertTrue(result.isEmpty());
    }

    // ===============================
    // ===== OVERWRITE AND DELETE =====
    // ===============================

    @Test
    void shouldOverwriteExistingRecipe() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));
        Recipe updated = createRecipe("1", "american pancakes", false);

        // when
        recipeRepository.saveRecipeForUser(USER_ID, updated);

        // then
        assertEquals(List.of(updated), recipeRepository.getRecipesForUser(USER_ID));
    }

    @Test
    void shouldDeleteRecipeForUser() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));

        // when
        recipeRepository.deleteRecipeForUser(USER_ID, "1");

        // then
        assertTrue(recipeRepository.getUserRecipeById(USER_ID, "1").isEmpty());
    }

    @Test
    void shouldDeleteOnlyGivenRecipe() {

        // given
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("1", "pancakes", false));
        recipeRepository.saveRecipeForUser(USER_ID, createRecipe("2", "soup", false));
        recipeRepository.saveRecipeForUser(OTHER_USER_ID, createRecipe("1", "salad", false));

        // when
        recipeRepository.deleteRecipeForUser(USER_ID, "1");

        // then
        assertEquals(List.of("2"), recipeIds(recipeRepository.getRecipesForUser(USER_ID)));
        assertTrue(recipeRepository.getUserRecipeById(OTHER_USER_ID, "1").isPresent());
    }

    @Test
    void shouldNotFailWhenDeletingMissingRecipe() {

        // when / then
        assertDoesNotThrow(() -> recipeRepository.deleteRecipeForUser(USER_ID, "missing"));
    }

    // ===================
    // ===== HELPERS =====
    // ===================

    // the repository has no write method for public recipes, so they are inserted directly
    private void savePublicRecipe(Recipe recipe) {
        var item = recipeMapper.toDynamoDBItem(PK_GLOBAL, "RECIPE#" + recipe.getRecipeId(), recipe);
        dynamoDBQueryHelper.saveSingleItem(item);
    }

    // BatchGetItem does not guarantee the order, so the ids are sorted before comparing
    private List<String> recipeIds(List<Recipe> recipes) {
        return recipes.stream().map(Recipe::getRecipeId).sorted().toList();
    }

    private Recipe createRecipe(String recipeId, String name, boolean isGlobal) {
        return new Recipe()
                .recipeId(recipeId)
                .name(name)
                .createdAt(OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z"))
                .updatedAt(OffsetDateTime.parse("2026-11-02T12:06:15.951250500Z"))
                .items(List.of(new RecipeItem("flour", 500f, "g", "Pantry / Dry Goods")))
                .isGlobal(isGlobal);
    }
}
