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
