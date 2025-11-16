package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.controllers.utils.ShoppingListCreator;
import com.jrakus.sl_main_service.repositories.RecipeRepository;
import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.*;
import org.openapitools.model.ShoppingList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class ShoppingListController implements ShoppingListsApi {

    private final ShoppingListRepository shoppingListRepository;
    private final RecipeRepository recipeRepository;

    private final ShoppingListCreator shoppingListCreator;

    public ShoppingListController(
            ShoppingListRepository shoppingListRepository,
            RecipeRepository recipeRepository,
            ShoppingListCreator shoppingListCreator
    ) {
        this.shoppingListRepository = shoppingListRepository;
        this.recipeRepository = recipeRepository;

        this.shoppingListCreator = shoppingListCreator;
    }

    @Override
    public ResponseEntity<List<ShoppingList>> getShoppingListsForUser(String userId) {

        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListsForUser(userId);
        return ResponseEntity.ok(shoppingLists);
    }

    @Override
    public ResponseEntity<ShoppingList> createShoppingList(String userId, ShoppingListCreate shoppingListCreate) {

        String newShoppingListId = UUID.randomUUID().toString();

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(newShoppingListId)
                .name(shoppingListCreate.getName())
                .itemsPerCategory(shoppingListCreate.getItemsPerCategory())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now());

        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);
        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> createShoppingListFromRecipes(String userId, ShoppingListCreateFromRecipes newShoppingListRequest) {
        String newShoppingListName = newShoppingListRequest.getName();
        List<String> userRecipeIds = newShoppingListRequest.getUserRecipeIds();
        List<String> publicRecipeIds = newShoppingListRequest.getPublicRecipeIds();

        List<Recipe> userRecipes = recipeRepository.getSpecificRecipesForUser(userId, userRecipeIds);
        List<Recipe> publicRecipes = recipeRepository.getSpecificPublicRecipes(publicRecipeIds);

        List<Recipe> allRecipes = new ArrayList<>(userRecipes);
        allRecipes.addAll(publicRecipes);

        ShoppingList shoppingList = shoppingListCreator.createShoppingList(newShoppingListName, allRecipes);
        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> deleteShoppingList(String userId, String shoppingListId) {

        // TODO
        // 1) Check if the element already exists
        // 2) Return the deleted item

        shoppingListRepository.deleteShoppingListForUser(userId, shoppingListId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ShoppingList> updateShoppingList(
            String userId, String shoppingListId, ShoppingListUpdate shoppingListBase
    ) {

        // TODO
        // Check if the element already exists
        // Do not update createdAt field - Reuse the value taken from DB

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(shoppingListId)
                .name(shoppingListBase.getName())
                .updatedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .itemsPerCategory(shoppingListBase.getItemsPerCategory());

        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        return ResponseEntity.ok(shoppingList);
    }
}
