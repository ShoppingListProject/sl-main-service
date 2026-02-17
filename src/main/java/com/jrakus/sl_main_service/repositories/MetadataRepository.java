package com.jrakus.sl_main_service.repositories;

import com.jrakus.sl_main_service.repositories.dynamo_db.models.ShoppingListMetadata;

import java.util.List;

public interface MetadataRepository {
    List<ShoppingListMetadata> getShoppingListMetadata(String userId);
    void saveShoppingListMetadata(String userId, List<ShoppingListMetadata> shoppingListMetadataList);
}
