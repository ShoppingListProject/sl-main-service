package com.jrakus.sl_main_service.api;

import org.junit.jupiter.api.Test;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListFromRecipesIntegrationTests extends ApiIntegrationTestBase {

    private static final String PATH = "/shoppingLists/fromRecipes/" + USER_ID;

    @Test
    void shouldCreateShoppingListFromUserRecipesMultiplyingQuantities() {

        // given
        Recipe pancakes = createRecipe(USER_ID, recipeCreate("pancakes",
                item("flour", 200f, "g", "Bakery"), item("milk", 0.5f, "l", "Dairy")));
        Recipe soup = createRecipe(USER_ID, recipeCreate("soup",
                item("milk", 0.25f, "l", "Dairy")));

        ShoppingListCreateFromRecipes request = fromRecipes("weekend",
                List.of(new RecipeIdWithNumber(pancakes.getRecipeId(), 2), new RecipeIdWithNumber(soup.getRecipeId(), 1)),
                List.of());

        // when
        ResponseEntity<ShoppingList> response = post(PATH, request, ShoppingList.class);

        // then
        assertEquals(201, response.getStatusCode().value());
        ShoppingList shoppingList = response.getBody();
        assertEquals("weekend", shoppingList.getName());
        assertEquals(List.of("Bakery", "Dairy"), shoppingList.getItemsPerCategory().stream().map(CategorizedItems::getCategory).toList());

        CategorizedItems bakery = shoppingList.getItemsPerCategory().get(0);
        assertEquals(1, bakery.getItems().size());
        assertEquals("flour", bakery.getItems().get(0).getName());
        assertEquals(400f, bakery.getItems().get(0).getQuantity());
        assertFalse(bakery.getItems().get(0).getPurchased());

        CategorizedItems dairy = shoppingList.getItemsPerCategory().get(1);
        assertEquals(2, dairy.getItems().size());
        assertTrue(dairy.getItems().stream().allMatch(listItem -> !listItem.getPurchased()));
    }

    @Test
    void shouldCreateShoppingListFromPublicRecipes() {

        // given
        publishPublicRecipes(publicRecipe("public-1", "public soup", item("carrot", 0.5f, "kg", "Vegetables & Fruits")));

        ShoppingListCreateFromRecipes request = fromRecipes("weekend",
                List.of(),
                List.of(new RecipeIdWithNumber("public-1", 3)));

        // when
        ResponseEntity<ShoppingList> response = post(PATH, request, ShoppingList.class);

        // then
        assertEquals(201, response.getStatusCode().value());
        CategorizedItems category = response.getBody().getItemsPerCategory().get(0);
        assertEquals("Vegetables & Fruits", category.getCategory());
        assertEquals("carrot", category.getItems().get(0).getName());
        assertEquals(1.5f, category.getItems().get(0).getQuantity());
    }

    @Test
    void shouldCreateShoppingListFromUserAndPublicRecipes() {

        // given
        Recipe own = createRecipe(USER_ID, recipeCreate("own", item("pork", 1f, "kg", "Meat")));
        publishPublicRecipes(publicRecipe("public-1", "public", item("carrot", 1f, "kg", "Vegetables & Fruits")));

        ShoppingListCreateFromRecipes request = fromRecipes("weekend",
                List.of(new RecipeIdWithNumber(own.getRecipeId(), 1)),
                List.of(new RecipeIdWithNumber("public-1", 1)));

        // when
        ResponseEntity<ShoppingList> response = post(PATH, request, ShoppingList.class);

        // then
        assertEquals(201, response.getStatusCode().value());
        assertEquals(2, response.getBody().getItemsPerCategory().size());
    }

    @Test
    void shouldPersistShoppingListAndAppendMetadata() {

        // given
        createShoppingList(USER_ID, shoppingListCreate("existing"));
        Recipe recipe = createRecipe(USER_ID, recipeCreate("pancakes", item("flour", 200f, "g", "Bakery")));

        // when
        ShoppingList created = post(PATH,
                fromRecipes("weekend", List.of(new RecipeIdWithNumber(recipe.getRecipeId(), 1)), List.of()),
                ShoppingList.class).getBody();

        // then
        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody();
        assertEquals(List.of("existing", "weekend"), metadata.stream().map(ShoppingListInfo::getShoppingListName).toList());
        assertEquals(created.getShoppingListId(), metadata.get(1).getId());

        List<ShoppingList> lists = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody();
        assertEquals(2, lists.size());
    }

    @Test
    void shouldCreateMetadataForFirstShoppingListOfUser() {

        // given
        Recipe recipe = createRecipe(USER_ID, recipeCreate("pancakes", item("flour", 200f, "g", "Bakery")));

        // when
        ShoppingList created = post(PATH,
                fromRecipes("first", List.of(new RecipeIdWithNumber(recipe.getRecipeId(), 1)), List.of()),
                ShoppingList.class).getBody();

        // then
        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody();
        assertEquals(1, metadata.size());
        assertEquals(created.getShoppingListId(), metadata.get(0).getId());
    }

    @Test
    void shouldReturnNotFoundAndPersistNothingWhenUserRecipeIsMissing() {

        // given
        Recipe recipe = createRecipe(USER_ID, recipeCreate("pancakes", item("flour", 200f, "g", "Bakery")));

        // when
        ResponseEntity<ErrorResponse> response = post(PATH,
                fromRecipes("weekend",
                        List.of(new RecipeIdWithNumber(recipe.getRecipeId(), 1), new RecipeIdWithNumber("missing", 1)),
                        List.of()),
                ErrorResponse.class);

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(Code.MANY_RECIPES_NOT_FOUND, response.getBody().getCode());
        assertTrue(get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody().isEmpty());
        assertTrue(get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody().isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenPublicRecipeIsMissing() {

        // when
        ResponseEntity<ErrorResponse> response = post(PATH,
                fromRecipes("weekend", List.of(), List.of(new RecipeIdWithNumber("missing", 1))),
                ErrorResponse.class);

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(Code.MANY_RECIPES_NOT_FOUND, response.getBody().getCode());
    }

    @Test
    void shouldNotUseRecipesOfAnotherUser() {

        // given
        Recipe recipe = createRecipe(OTHER_USER_ID, recipeCreate("pancakes", item("flour", 200f, "g", "Bakery")));

        // when
        ResponseEntity<ErrorResponse> response = post(PATH,
                fromRecipes("weekend", List.of(new RecipeIdWithNumber(recipe.getRecipeId(), 1)), List.of()),
                ErrorResponse.class);

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(Code.MANY_RECIPES_NOT_FOUND, response.getBody().getCode());
    }

    private static ShoppingListCreateFromRecipes fromRecipes(
            String name, List<RecipeIdWithNumber> userRecipes, List<RecipeIdWithNumber> publicRecipes
    ) {
        ShoppingListCreateFromRecipes request = new ShoppingListCreateFromRecipes().name(name);
        userRecipes.forEach(request::addUserRecipeArrayItem);
        publicRecipes.forEach(request::addPublicRecipeArrayItem);
        return request;
    }
}
