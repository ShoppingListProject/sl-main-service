package com.jrakus.sl_main_service.api;

import org.junit.jupiter.api.Test;
import org.openapitools.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserJourneyIntegrationTests extends ApiIntegrationTestBase {

    @Test
    void shouldKeepMetadataAndListingsConsistentThroughoutUserJourney() {

        // given - the user creates two recipes
        Recipe pancakes = createRecipe(USER_ID, recipeCreate("pancakes",
                item("flour", 200f, "g", "Bakery"), item("milk", 0.5f, "l", "Dairy")));
        Recipe soup = createRecipe(USER_ID, recipeCreate("soup", item("carrot", 0.5f, "kg", "Vegetables & Fruits")));
        assertEquals(2, get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody().size());

        // when - a shopping list is created from them
        ShoppingListCreateFromRecipes request = new ShoppingListCreateFromRecipes()
                .name("weekend")
                .addUserRecipeArrayItem(new RecipeIdWithNumber(pancakes.getRecipeId(), 2))
                .addUserRecipeArrayItem(new RecipeIdWithNumber(soup.getRecipeId(), 1));
        ShoppingList shoppingList = post("/shoppingLists/fromRecipes/" + USER_ID, request, ShoppingList.class).getBody();

        // then
        assertEquals(3, shoppingList.getItemsPerCategory().size());

        // when - the list is edited (an item is marked as purchased)
        ShoppingListCreate update = shoppingListCreate("weekend shopping",
                category("Bakery", listItem("flour", 400f, "g", true)));
        put("/shoppingLists/" + USER_ID + "/" + shoppingList.getShoppingListId(), update, ShoppingList.class);

        // then
        List<ShoppingList> lists = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody();
        assertEquals(1, lists.size());
        assertEquals("weekend shopping", lists.get(0).getName());
        assertEquals("weekend shopping",
                get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody().get(0).getShoppingListName());

        // when - a recipe is deleted
        delete("/recipes/" + USER_ID + "/" + pancakes.getRecipeId(), Recipe.class);

        // then - the recipe is gone, but the shopping list created from it stays
        assertEquals(List.of(soup.getRecipeId()),
                get("/recipes/metadata/" + USER_ID, RECIPE_INFO_LIST).getBody().stream().map(RecipeInfo::getId).toList());
        assertEquals(1, get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody().size());

        // when - the shopping list is deleted
        delete("/shoppingLists/" + USER_ID + "/" + shoppingList.getShoppingListId(), ShoppingList.class);

        // then
        assertTrue(get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody().isEmpty());
        assertTrue(get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody().isEmpty());
        assertEquals(0, get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=10", NumberOfPages.class).getBody().getNumberOfPages());
    }
}
