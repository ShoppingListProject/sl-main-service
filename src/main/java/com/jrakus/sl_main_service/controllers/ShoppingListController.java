package com.jrakus.sl_main_service.controllers;

import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.ShoppingList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ShoppingListController implements ShoppingListsApi {

    @Override
    public ResponseEntity<List<ShoppingList>> getShoppingListForUser(Integer userId) {

        List<ShoppingList> shoppingListArray = List.of(
                new ShoppingList()
        );

        return ResponseEntity.ok(shoppingListArray);
    }
}
