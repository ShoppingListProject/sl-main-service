package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingList;

import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {

    List<ShoppingList> getShoppingListsForUser(String userId);

    Optional<ShoppingList> getUserShoppingListById(String userId, String shoppingListId);

    void saveShoppingListForUser(String userId, ShoppingList shoppingList);

    void deleteShoppingListForUser(String userId, String shoppingListId);
}
