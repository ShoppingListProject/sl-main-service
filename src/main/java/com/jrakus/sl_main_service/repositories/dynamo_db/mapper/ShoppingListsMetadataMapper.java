package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import com.jrakus.sl_main_service.repositories.dynamo_db.models.ShoppingListMetadata;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ShoppingListsMetadataMapper {

    // ======================
    // ======== READ ========
    // ======================

    public List<ShoppingListMetadata> fromDynamoDBItem(Map<String, AttributeValue> item) {
        return mapToShoppingListMetadataList(item.get("metadata").l());
    }

    private List<ShoppingListMetadata> mapToShoppingListMetadataList(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToShoppingListMetadata)
                .toList();
    }

    private ShoppingListMetadata mapToShoppingListMetadata(Map<String, AttributeValue> item) {

        String shoppingListName = item.get("shoppingListName").s();
        String id = item.get("id").s();
        OffsetDateTime updatedAt = OffsetDateTime.parse(item.get("updatedAt").s());

        return new ShoppingListMetadata(shoppingListName, id, updatedAt);
    }

    // =======================
    // ======== WRITE ========
    // =======================

    public Map<String, AttributeValue> toDynamoDBItem(
            String pk,
            String sk,
            List<ShoppingListMetadata> shoppingListMetadataList
    ) {
        return Map.of(
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "name", AttributeValue.builder().l(mapShoppingListMetadataList(shoppingListMetadataList)).build()
        );
    }

    private List<AttributeValue> mapShoppingListMetadataList(List<ShoppingListMetadata> shoppingListMetadataList) {
        return shoppingListMetadataList.stream()
                .map(this::mapShoppingListMetadata)
                .toList();
    }

    private AttributeValue mapShoppingListMetadata(ShoppingListMetadata shoppingListMetadata) {

        AttributeValue shoppingListName = AttributeValue.builder().s(
                shoppingListMetadata.shoppingListName()
        ).build();

        AttributeValue id = AttributeValue.builder().s(
                shoppingListMetadata.id()
        ).build();

        AttributeValue updatedAt = AttributeValue.builder().s(
                shoppingListMetadata.updatedAt().toString()
        ).build();

        return AttributeValue.builder()
                .m(Map.of(
                        "shoppingListName", shoppingListName,
                        "id", id,
                        "updatedAt", updatedAt
                ))
                .build();
    }
}
