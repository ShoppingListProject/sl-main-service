package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.ShoppingListInfo;

import java.util.List;

public interface MetadataRepository {
    List<ShoppingListInfo> getShoppingListMetadata(String userId);
    void saveShoppingListMetadata(String userId, List<ShoppingListInfo> shoppingListMetadataList);
}
