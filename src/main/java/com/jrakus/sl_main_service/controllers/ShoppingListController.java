package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.ShoppingList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ShoppingListController implements ShoppingListsApi {

    private final ShoppingListRepository shoppingListRepository;

    public ShoppingListController(ShoppingListRepository shoppingListRepository) {
        this.shoppingListRepository = shoppingListRepository;
    }

    @Override
    public ResponseEntity<List<ShoppingList>> getShoppingListForUser(String userId) {

        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListsForUser(userId);

        return ResponseEntity.ok(shoppingLists);
    }
}
