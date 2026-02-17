package com.jrakus.sl_main_service.repositories.dynamo_db.models;

import java.time.OffsetDateTime;

public record ShoppingListMetadata(String shoppingListName, String id, OffsetDateTime updatedAt) {
}
