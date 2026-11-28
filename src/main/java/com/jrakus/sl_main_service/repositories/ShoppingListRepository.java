package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository {

    List<ShoppingList> getShoppingListsForUser(String userId, List<String> shoppingListIdList);

    Optional<ShoppingList> getUserShoppingListById(String userId, String shoppingListId);

    void saveShoppingListForUser(String userId, ShoppingList shoppingList);

    void deleteShoppingListForUser(String userId, String shoppingListId);

    /**
     * Sets the purchased flag of the item at the given position.
     * Returns false when the shopping list or the item position doesn't exist.
     */
    boolean updateItemPurchasedForUser(
            String userId,
            String shoppingListId,
            int categoryIndex,
            int itemIndex,
            boolean purchased,
            OffsetDateTime updatedAt
    );
}
