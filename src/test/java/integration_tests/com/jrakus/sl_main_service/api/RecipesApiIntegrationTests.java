package com.jrakus.sl_main_service.api;

import org.junit.jupiter.api.Test;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipesApiIntegrationTests extends ApiIntegrationTestBase {

    // ======================
    // ===== CREATE =========
    // ======================

    @Test
    void shouldCreateRecipeAndReturnIt() {

        // given
        RecipeCreate request = recipeCreate("pancakes", item("flour", 200f, "g", "Bakery"));

        // when
        ResponseEntity<Recipe> response = post("/recipes/" + USER_ID, request, Recipe.class);

        // then
        assertEquals(201, response.getStatusCode().value());
        Recipe recipe = response.getBody();
        assertNotNull(recipe.getRecipeId());
        assertEquals("pancakes", recipe.getName());
        assertEquals(1, recipe.getItems().size());
        assertEquals("flour", recipe.getItems().get(0).getName());
        assertNotNull(recipe.getCreatedAt());
        assertEquals(recipe.getCreatedAt(), recipe.getUpdatedAt());
    }

    @Test
    void shouldAddCreatedRecipeToMetadataAndList() {

        // given
        Recipe recipe = createRecipe(USER_ID, recipeCreate("pancakes"));

        // when
        List<RecipeInfo> metadata = get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody();
        List<Recipe> recipes = get("/recipes/" + USER_ID + "?areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();

        // then
        assertEquals(1, metadata.size());
        assertEquals(recipe.getRecipeId(), metadata.get(0).getId());
        assertEquals("pancakes", metadata.get(0).getRecipeName());
        assertFalse(metadata.get(0).getIsGlobal());

        assertEquals(1, recipes.size());
        assertEquals(recipe.getRecipeId(), recipes.get(0).getRecipeId());
    }

    // ======================
    // ===== UPDATE =========
    // ======================

    @Test
    void shouldUpdateRecipeAndItsMetadata() {

        // given
        Recipe created = createRecipe(USER_ID, recipeCreate("pancakes", item("flour", 200f, "g", "Bakery")));
        createRecipe(USER_ID, recipeCreate("soup"));

        // when
        ResponseEntity<Recipe> response = put(
                "/recipes/" + USER_ID + "/" + created.getRecipeId(),
                recipeCreate("crepes", item("milk", 1f, "l", "Dairy")),
                Recipe.class
        );

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("crepes", response.getBody().getName());
        assertEquals(created.getCreatedAt(), response.getBody().getCreatedAt());
        assertEquals("milk", response.getBody().getItems().get(0).getName());

        List<RecipeInfo> metadata = get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody();
        assertEquals(2, metadata.size());
        assertEquals(List.of("crepes", "soup"), metadata.stream().map(RecipeInfo::getRecipeName).toList());

        Recipe stored = get("/recipes/" + USER_ID + "?areGlobalRecipesIncluded=false", RECIPE_LIST).getBody().stream()
                .filter(recipe -> recipe.getRecipeId().equals(created.getRecipeId()))
                .findFirst().orElseThrow();
        assertEquals("crepes", stored.getName());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownRecipe() {

        // when
        ResponseEntity<String> response = put("/recipes/" + USER_ID + "/unknown", recipeCreate("crepes"), String.class);

        // then
        assertEquals(404, response.getStatusCode().value());
    }

    // ======================
    // ===== DELETE =========
    // ======================

    @Test
    void shouldDeleteRecipeAndRemoveItFromMetadata() {

        // given
        Recipe pancakes = createRecipe(USER_ID, recipeCreate("pancakes"));
        Recipe soup = createRecipe(USER_ID, recipeCreate("soup"));

        // when
        ResponseEntity<Recipe> response = delete("/recipes/" + USER_ID + "/" + pancakes.getRecipeId(), Recipe.class);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(pancakes.getRecipeId(), response.getBody().getRecipeId());

        List<RecipeInfo> metadata = get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody();
        assertEquals(List.of(soup.getRecipeId()), metadata.stream().map(RecipeInfo::getId).toList());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownRecipe() {

        // when
        ResponseEntity<String> response = delete("/recipes/" + USER_ID + "/unknown", String.class);

        // then
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void shouldNotDeleteRecipeOfAnotherUser() {

        // given
        Recipe recipe = createRecipe(USER_ID, recipeCreate("pancakes"));

        // when
        ResponseEntity<String> response = delete("/recipes/" + OTHER_USER_ID + "/" + recipe.getRecipeId(), String.class);

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(1, get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody().size());
    }

    // ======================
    // ===== LIST ===========
    // ======================

    @Test
    void shouldReturnEmptyListForUserWithoutRecipes() {

        // when
        ResponseEntity<List<Recipe>> response = get("/recipes/" + USER_ID, RECIPE_LIST);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldPaginateRecipes() {

        // given
        for (int i = 1; i <= 5; i++) {
            createRecipe(USER_ID, recipeCreate("recipe-" + i));
        }

        // when
        List<Recipe> firstPage = get("/recipes/" + USER_ID + "?offset=0&limit=2&areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();
        List<Recipe> lastPage = get("/recipes/" + USER_ID + "?offset=4&limit=2&areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();
        List<Recipe> beyondEnd = get("/recipes/" + USER_ID + "?offset=10&limit=2&areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();

        // then
        assertEquals(2, firstPage.size());
        assertEquals(1, lastPage.size());
        assertTrue(beyondEnd.isEmpty());
    }

    @Test
    void shouldRejectInvalidPaginationParameters() {

        // when / then
        assertEquals(400, get("/recipes/" + USER_ID + "?offset=-1", String.class).getStatusCode().value());
        assertEquals(400, get("/recipes/" + USER_ID + "?limit=0", String.class).getStatusCode().value());
        assertEquals(400, get("/recipes/" + USER_ID + "?limit=101", String.class).getStatusCode().value());
    }

    @Test
    void shouldSearchRecipesIgnoringCase() {

        // given
        createRecipe(USER_ID, recipeCreate("Pancakes"));
        createRecipe(USER_ID, recipeCreate("Soup"));

        // when
        List<Recipe> recipes = get("/recipes/" + USER_ID + "?querySearch=PANCA&areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();

        // then
        assertEquals(1, recipes.size());
        assertEquals("Pancakes", recipes.get(0).getName());
    }

    @Test
    void shouldIncludeGlobalRecipesOnlyWhenRequested() {

        // given
        createRecipe(USER_ID, recipeCreate("my soup"));
        publishPublicRecipes(publicRecipe("public-1", "public soup"));

        // when
        List<Recipe> withGlobal = get("/recipes/" + USER_ID + "?areGlobalRecipesIncluded=true", RECIPE_LIST).getBody();
        List<Recipe> withoutGlobal = get("/recipes/" + USER_ID + "?areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();

        // then
        assertEquals(List.of("my soup", "public soup"), withGlobal.stream().map(Recipe::getName).toList());
        assertEquals(List.of("my soup"), withoutGlobal.stream().map(Recipe::getName).toList());
    }

    @Test
    void shouldNotShowRecipesOfAnotherUser() {

        // given
        createRecipe(USER_ID, recipeCreate("pancakes"));

        // when
        List<Recipe> recipes = get("/recipes/" + OTHER_USER_ID + "?areGlobalRecipesIncluded=false", RECIPE_LIST).getBody();
        List<RecipeInfo> metadata = get("/recipes/metadata/" + OTHER_USER_ID, RECIPE_INFO_LIST).getBody();

        // then
        assertTrue(recipes.isEmpty());
        assertTrue(metadata.isEmpty());
    }

    // ======================
    // ===== PAGES ==========
    // ======================

    @Test
    void shouldCountPagesIncludingGlobalRecipes() {

        // given
        for (int i = 1; i <= 3; i++) {
            createRecipe(USER_ID, recipeCreate("recipe-" + i));
        }
        publishPublicRecipes(publicRecipe("public-1", "public-1"), publicRecipe("public-2", "public-2"));

        // when
        NumberOfPages withGlobal = get("/recipes/pages/" + USER_ID + "?itemsPerPage=2&areGlobalRecipesIncluded=true", NumberOfPages.class).getBody();
        NumberOfPages withoutGlobal = get("/recipes/pages/" + USER_ID + "?itemsPerPage=2&areGlobalRecipesIncluded=false", NumberOfPages.class).getBody();

        // then
        assertEquals(3, withGlobal.getNumberOfPages());
        assertEquals(2, withoutGlobal.getNumberOfPages());
    }

    @Test
    void shouldCountPagesMatchingSearch() {

        // given
        createRecipe(USER_ID, recipeCreate("Pancakes"));
        createRecipe(USER_ID, recipeCreate("Soup"));

        // when
        NumberOfPages pages = get("/recipes/pages/" + USER_ID + "?itemsPerPage=1&querySearch=soup&areGlobalRecipesIncluded=false", NumberOfPages.class).getBody();

        // then
        assertEquals(1, pages.getNumberOfPages());
    }

    @Test
    void shouldRejectInvalidItemsPerPage() {

        // when / then
        assertEquals(400, get("/recipes/pages/" + USER_ID + "?itemsPerPage=abc", String.class).getStatusCode().value());
        assertEquals(400, get("/recipes/pages/" + USER_ID + "?itemsPerPage=0", String.class).getStatusCode().value());
        assertEquals(400, get("/recipes/pages/" + USER_ID, String.class).getStatusCode().value());
    }
}
