package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.controllers.utils.ShoppingListCreator;
import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import jakarta.validation.Valid;
import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.NewShoppingListRequest;
import org.openapitools.model.Recipe;
import org.openapitools.model.ShoppingList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ShoppingListController implements ShoppingListsApi {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListCreator shoppingListCreator;

    public ShoppingListController(
            ShoppingListRepository shoppingListRepository,
            ShoppingListCreator shoppingListCreator
    ) {
        this.shoppingListRepository = shoppingListRepository;
        this.shoppingListCreator = shoppingListCreator;
    }

    @Override
    public ResponseEntity<List<ShoppingList>> getShoppingListsForUser(String userId) {

        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListsForUser(userId);

        return ResponseEntity.ok(shoppingLists);
    }

    @Override
    public ResponseEntity<ShoppingList> createShoppingList(String userId, NewShoppingListRequest newShoppingListRequest) {

        String newShoppingListName = newShoppingListRequest.getName();
        List<Recipe> recipes = newShoppingListRequest.getRecipes();

        ShoppingList newShoppingList = shoppingListCreator.createShoppingList(newShoppingListName, recipes);

        return ResponseEntity.ok(newShoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> deleteShoppingList(String userId, String shoppingListId) {
        return null;
    }

    @Override
    public ResponseEntity<ShoppingList> updateShoppingList(String userId, String shoppingListId, ShoppingList shoppingList) {
        return null;
    }
}
