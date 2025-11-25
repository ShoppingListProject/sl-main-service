package com.jrakus.sl_main_service.controllers.utils;

import org.openapitools.model.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
public class ShoppingListCreator {

    public ShoppingList createShoppingList(String shoppingListName, List<Recipe> recipes) {

        String newShoppingListId = UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(newShoppingListId)
                .name(shoppingListName)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now());

        List<CategorizedItems> itemsPerCategory = getItemsPerCategory(recipes);
        shoppingList.setItemsPerCategory(itemsPerCategory);

        return shoppingList;
    }

    public List<Recipe> prepareRecipes(
            List<Recipe> userRecipes,
            List<Recipe> publicRecipes,
            List<RecipeIdWithNumber> userRecipeArray,
            List<RecipeIdWithNumber> publicRecipeArray) {

        // We need to sort items because user could return it in any order, so did DynamoDB.
        sortRecipeArrays(userRecipes, publicRecipes, userRecipeArray, publicRecipeArray);

        Map<Recipe, Integer> recipeToAmount = createMapFromRecipeToAmount(
                userRecipes, publicRecipes, userRecipeArray, publicRecipeArray
        );

        return createRecipesWithCorrectAmountOfItems(recipeToAmount);
    };

    private void sortRecipeArrays(
            List<Recipe> userRecipes,
            List<Recipe> publicRecipes,
            List<RecipeIdWithNumber> userRecipeArray,
            List<RecipeIdWithNumber> publicRecipeArray
    ) {
        userRecipes.sort(Comparator.comparing(Recipe::getRecipeId));
        publicRecipes.sort(Comparator.comparing(Recipe::getRecipeId));
        userRecipeArray.sort(Comparator.comparing(RecipeIdWithNumber::getRecipeId));
        publicRecipeArray.sort(Comparator.comparing(RecipeIdWithNumber::getRecipeId));
    }

    private Map<Recipe, Integer> createMapFromRecipeToAmount(
            List<Recipe> userRecipes,
            List<Recipe> publicRecipes,
            List<RecipeIdWithNumber> userRecipeArray,
            List<RecipeIdWithNumber> publicRecipeArray
    ) {

        Map<Recipe, Integer> recipeToAmount = new HashMap<>();

        // As we have sorted items, and we are sure that their lengths are equal we can easily match Recipe to amount
        for(int index = 0; index < userRecipes.size(); index++) {

            Recipe recipe = userRecipes.get(index);
            int amount = userRecipeArray.get(index).getAmount();

            recipeToAmount.put(recipe, amount);
        }

        for(int index = 0; index < publicRecipes.size(); index++) {

            Recipe recipe = publicRecipes.get(index);
            int amount = publicRecipeArray.get(index).getAmount();

            recipeToAmount.put(recipe, amount);
        }

        return recipeToAmount;
    }

    private List<Recipe> createRecipesWithCorrectAmountOfItems(Map<Recipe, Integer> recipeToAmount) {

        List<Recipe> allPreparedRecipes = new ArrayList<>(recipeToAmount.size());

        for(Recipe unpreparedRecipe: recipeToAmount.keySet()) {

            int amount = recipeToAmount.get(unpreparedRecipe);
            Recipe preparedRecipe = multiplyRecipeByAmount(unpreparedRecipe, amount);

            allPreparedRecipes.add(preparedRecipe);
        }

        return allPreparedRecipes;
    }

    private List<CategorizedItems> getItemsPerCategory(List<Recipe> recipes) {
        Map<String, List<RecipeItem>> recipeItemsPerCategory = getRecipeItemsPerCategory(recipes);

        List<CategorizedItems> itemsPerCategory = new ArrayList<>();
        Set<String> categories = recipeItemsPerCategory.keySet();

        for(String category: categories) {

            CategorizedItems categorizedItems = new CategorizedItems().category(category);

            List<RecipeItem> recipeItems = recipeItemsPerCategory.get(category);
            List<ShoppingListItem> shoppingListItems = mapRecipeItemsToShoppingListItems(recipeItems);

            categorizedItems.setItems(shoppingListItems);
            itemsPerCategory.add(categorizedItems);
        }

        itemsPerCategory.sort(
                Comparator.comparing(CategorizedItems::getCategory)
        );

        return itemsPerCategory;
    }

    private Map<String, List<RecipeItem>> getRecipeItemsPerCategory(List<Recipe> recipes) {

        List<String> distinctCategories = getDistinctCategories(recipes);
        Map<String, List<RecipeItem>> recipeItemsPerCategory = new HashMap<>();

        // For each category create empty list;
        for(String category: distinctCategories) {
            recipeItemsPerCategory.put(category, new ArrayList<>());
        }

        List<RecipeItem> recipeItems = recipes.stream()
                .flatMap(recipe -> recipe.getItems().stream())
                .toList();

        // Add recipeItem to appropriate category;
        for(RecipeItem recipeItem: recipeItems) {
            List<RecipeItem> categorizedRecipeItems = recipeItemsPerCategory.get(recipeItem.getCategory());
            categorizedRecipeItems.add(recipeItem);
        }

        return recipeItemsPerCategory;
    }

    private Recipe multiplyRecipeByAmount(Recipe unpreparedRecipe, int amount) {

        unpreparedRecipe.getItems().forEach(item -> {

            float quantity = item.getQuantity() * amount;
            float roundedQuantity =  Math.round(quantity * 100.0) / 100.0f;

            item.setQuantity(roundedQuantity);
        });

        return unpreparedRecipe;
    }

    private List<String> getDistinctCategories(List<Recipe> recipes) {
        return recipes.stream()
                .flatMap(recipe -> recipe.getItems().stream())
                .map(RecipeItem::getCategory)
                .distinct()
                .toList();
    }

    private List<ShoppingListItem> mapRecipeItemsToShoppingListItems(List<RecipeItem> recipeItems) {

        List<ShoppingListItem> shoppingListItems = new ArrayList<>();

        for(RecipeItem recipeItem: recipeItems) {
            ShoppingListItem shoppingListItem = new ShoppingListItem()
                    .name(recipeItem.getName())
                    .unit(recipeItem.getUnit())
                    .quantity(recipeItem.getQuantity())
                    .purchased(false);

            shoppingListItems.add(shoppingListItem);
        }

        shoppingListItems.sort(
                Comparator.comparing(ShoppingListItem::getName)
        );

        return shoppingListItems;
    }
}
