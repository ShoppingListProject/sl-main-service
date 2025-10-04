package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingList;

import java.util.List;

public interface ShoppingListRepository {
    List<ShoppingList> getAllShoppingListsForUser(String userId);
}
