package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.ShoppingListInfo;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShoppingListsMetadataMapper {

    // ======================
    // ======== READ ========
    // ======================

    public List<ShoppingListInfo> fromDynamoDBItem(Map<String, AttributeValue> item) {
        return mapToShoppingListMetadataList(item.get("metadata").l());
    }

    private List<ShoppingListInfo> mapToShoppingListMetadataList(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToShoppingListMetadata)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private ShoppingListInfo mapToShoppingListMetadata(Map<String, AttributeValue> item) {

        String shoppingListName = item.get("shoppingListName").s();
        String id = item.get("id").s();
        OffsetDateTime updatedAt = OffsetDateTime.parse(item.get("updatedAt").s());

        return new ShoppingListInfo(shoppingListName, id, updatedAt);
    }

    // =======================
    // ======== WRITE ========
    // =======================

    public Map<String, AttributeValue> toDynamoDBItem(
            String pk,
            String sk,
            List<ShoppingListInfo> shoppingListMetadataList
    ) {
        return Map.of(
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "metadata", AttributeValue.builder().l(mapShoppingListMetadataList(shoppingListMetadataList)).build()
        );
    }

    private List<AttributeValue> mapShoppingListMetadataList(List<ShoppingListInfo> shoppingListMetadataList) {
        return shoppingListMetadataList.stream()
                .map(this::mapShoppingListMetadata)
                .toList();
    }

    private AttributeValue mapShoppingListMetadata(ShoppingListInfo shoppingListMetadata) {

        AttributeValue shoppingListName = AttributeValue.builder().s(
                shoppingListMetadata.getShoppingListName()
        ).build();

        AttributeValue id = AttributeValue.builder().s(
                shoppingListMetadata.getId()
        ).build();

        AttributeValue updatedAt = AttributeValue.builder().s(
                shoppingListMetadata.getUpdatedAt().toString()
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
