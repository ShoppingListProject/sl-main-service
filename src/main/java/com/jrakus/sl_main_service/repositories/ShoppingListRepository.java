package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListResponse;

import java.util.List;

public interface ShoppingListRepository {

    List<ShoppingList> getShoppingListsForUser(String userId);

    void saveShoppingListForUser(String userId, ShoppingListResponse shoppingList);
}
