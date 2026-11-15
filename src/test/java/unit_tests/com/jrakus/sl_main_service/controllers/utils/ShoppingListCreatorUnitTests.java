package com.jrakus.sl_main_service.controllers.utils;

import org.junit.jupiter.api.Test;
import org.openapitools.model.*;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListCreatorUnitTests {

    private static final String SHOPPING_LIST_NAME = "myShoppingList";
    private static final OffsetDateTime TIME = OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z");

    private final ShoppingListCreator shoppingListCreator = new ShoppingListCreator();

    @Test
    void shouldCreateShoppingListWithNameIdAndEqualTimestamps() {

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, List.of());

        // then
        assertEquals(SHOPPING_LIST_NAME, shoppingList.getName());
        assertNotNull(shoppingList.getShoppingListId());
        assertDoesNotThrow(() -> java.util.UUID.fromString(shoppingList.getShoppingListId()));
        assertNotNull(shoppingList.getCreatedAt());
        assertEquals(shoppingList.getCreatedAt(), shoppingList.getUpdatedAt());
    }

    @Test
    void shouldGenerateDifferentIdsForEachShoppingList() {

        // when
        ShoppingList first = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, List.of());
        ShoppingList second = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, List.of());

        // then
        assertNotEquals(first.getShoppingListId(), second.getShoppingListId());
    }

    @Test
    void shouldCreateEmptyItemsPerCategoryForNoRecipes() {

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, List.of());

        // then
        assertTrue(shoppingList.getItemsPerCategory().isEmpty());
    }

    @Test
    void shouldCreateEmptyItemsPerCategoryForRecipesWithoutItems() {

        // given
        List<Recipe> recipes = List.of(recipe("1", false), recipe("2", true));

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, recipes);

        // then
        assertTrue(shoppingList.getItemsPerCategory().isEmpty());
    }

    @Test
    void shouldGroupItemsByCategoryAcrossRecipes() {

        // given
        List<Recipe> recipes = List.of(
                recipe("1", false, item("cucumber", 1f, "kg", "vegetables"), item("pork", 700f, "g", "meat")),
                recipe("2", true, item("carrot", 0.5f, "kg", "vegetables"))
        );

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, recipes);

        // then
        List<CategorizedItems> categories = shoppingList.getItemsPerCategory();
        assertEquals(2, categories.size());
        assertEquals(2, categories.stream().filter(c -> c.getCategory().equals("vegetables")).findFirst().orElseThrow().getItems().size());
        assertEquals(1, categories.stream().filter(c -> c.getCategory().equals("meat")).findFirst().orElseThrow().getItems().size());
    }

    @Test
    void shouldSortCategoriesAndItemsAlphabetically() {

        // given
        List<Recipe> recipes = List.of(
                recipe("1", false,
                        item("water", 3f, "l", "beverages"),
                        item("pork", 700f, "g", "meat"),
                        item("cucumber", 1f, "kg", "vegetables"),
                        item("carrot", 0.1f, "kg", "vegetables"))
        );

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, recipes);

        // then
        List<CategorizedItems> categories = shoppingList.getItemsPerCategory();
        assertEquals(List.of("beverages", "meat", "vegetables"),
                categories.stream().map(CategorizedItems::getCategory).toList());
        assertEquals(List.of("carrot", "cucumber"),
                categories.get(2).getItems().stream().map(ShoppingListItem::getName).toList());
    }

    @Test
    void shouldMapRecipeItemToUnpurchasedShoppingListItem() {

        // given
        List<Recipe> recipes = List.of(recipe("1", false, item("cucumber", 1.5f, "kg", "vegetables")));

        // when
        ShoppingList shoppingList = shoppingListCreator.createShoppingList(SHOPPING_LIST_NAME, recipes);

        // then
        ShoppingListItem result = shoppingList.getItemsPerCategory().getFirst().getItems().getFirst();
        assertEquals("cucumber", result.getName());
        assertEquals(1.5f, result.getQuantity());
        assertEquals("kg", result.getUnit());
        assertFalse(result.getPurchased());
    }

    // prepareRecipes

    @Test
    void shouldReturnEmptyListWhenNoRecipesToPrepare() {

        // when
        List<Recipe> result = shoppingListCreator.prepareRecipes(List.of(), List.of(), List.of(), List.of());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMultiplyItemQuantitiesByAmount() {

        // given
        List<Recipe> userRecipes = List.of(recipe("1", false, item("cucumber", 2f, "kg", "vegetables")));
        List<Recipe> publicRecipes = List.of(recipe("2", true, item("pork", 100f, "g", "meat")));

        // when
        List<Recipe> result = shoppingListCreator.prepareRecipes(
                userRecipes, publicRecipes, List.of(amount("1", 3)), List.of(amount("2", 2)));

        // then
        assertEquals(2, result.size());
        assertEquals(6f, findById(result, "1").getItems().getFirst().getQuantity());
        assertEquals(200f, findById(result, "2").getItems().getFirst().getQuantity());
    }

    @Test
    void shouldRoundMultipliedQuantitiesToTwoDecimalPlaces() {

        // given
        List<Recipe> publicRecipes = List.of(recipe("1", true, item("carrot", 0.333f, "kg", "vegetables")));

        // when
        List<Recipe> result = shoppingListCreator.prepareRecipes(
                List.of(), publicRecipes, List.of(), List.of(amount("1", 1)));

        // then
        assertEquals(0.33f, result.getFirst().getItems().getFirst().getQuantity());
    }

    @Test
    void shouldMatchPublicRecipesToAmountsRegardlessOfOrder() {

        // given
        List<Recipe> publicRecipes = List.of(
                recipe("c", true, item("pork", 1f, "kg", "meat")),
                recipe("a", true, item("pork", 1f, "kg", "meat")),
                recipe("b", true, item("pork", 1f, "kg", "meat"))
        );
        List<RecipeIdWithNumber> amounts = List.of(amount("b", 2), amount("c", 3), amount("a", 1));

        // when
        List<Recipe> result = shoppingListCreator.prepareRecipes(List.of(), publicRecipes, List.of(), amounts);

        // then
        assertEquals(1f, findById(result, "a").getItems().getFirst().getQuantity());
        assertEquals(2f, findById(result, "b").getItems().getFirst().getQuantity());
        assertEquals(3f, findById(result, "c").getItems().getFirst().getQuantity());
    }

    @Test
    void shouldMatchUserRecipesToAmountsRegardlessOfOrder() {

        // given
        List<Recipe> userRecipes = List.of(
                recipe("c", false, item("cucumber", 1f, "kg", "vegetables")),
                recipe("a", false, item("cucumber", 1f, "kg", "vegetables")),
                recipe("b", false, item("cucumber", 1f, "kg", "vegetables"))
        );
        List<RecipeIdWithNumber> amounts = List.of(amount("b", 2), amount("c", 3), amount("a", 1));

        // when
        List<Recipe> result = shoppingListCreator.prepareRecipes(userRecipes, List.of(), amounts, List.of());

        // then
        assertEquals(1f, findById(result, "a").getItems().getFirst().getQuantity());
        assertEquals(2f, findById(result, "b").getItems().getFirst().getQuantity());
        assertEquals(3f, findById(result, "c").getItems().getFirst().getQuantity());
    }

    @Test
    void shouldNotModifyInputListsWhenPreparingRecipes() {

        // given
        List<Recipe> userRecipes = List.of(recipe("b", false), recipe("a", false));
        List<Recipe> publicRecipes = List.of(recipe("d", true), recipe("c", true));
        List<RecipeIdWithNumber> userAmounts = List.of(amount("b", 1), amount("a", 1));
        List<RecipeIdWithNumber> publicAmounts = List.of(amount("d", 1), amount("c", 1));

        // when / then (immutable lists would throw on in-place sort)
        assertDoesNotThrow(() -> shoppingListCreator.prepareRecipes(userRecipes, publicRecipes, userAmounts, publicAmounts));
        assertEquals("b", userRecipes.getFirst().getRecipeId());
        assertEquals("d", publicRecipes.getFirst().getRecipeId());
    }

   // ==================== Helper methods ====================

    private static RecipeItem item(String name, float quantity, String unit, String category) {
        return new RecipeItem(name, quantity, unit, category);
    }

    private static Recipe recipe(String recipeId, boolean isGlobal, RecipeItem... items) {
        return new Recipe("recipe" + recipeId, List.of(items), isGlobal, recipeId, TIME, TIME);
    }

    private static RecipeIdWithNumber amount(String recipeId, int amount) {
        return new RecipeIdWithNumber(recipeId, amount);
    }

    private static Recipe findById(List<Recipe> recipes, String recipeId) {
        return recipes.stream()
                .filter(recipe -> recipe.getRecipeId().equals(recipeId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Recipe not found: " + recipeId));
    }
}
