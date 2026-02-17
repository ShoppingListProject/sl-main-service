package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {

    List<ShoppingList> getShoppingListsForUser(String userId, OffsetDateTime from, OffsetDateTime to);

    Optional<ShoppingList> getUserShoppingListById(String userId, String shoppingListId);

    void saveShoppingListForUser(String userId, ShoppingList shoppingList);

    void deleteShoppingListForUser(String userId, String shoppingListId);
}
