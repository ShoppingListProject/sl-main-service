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
                "PK", AttributeValue.fromS(pk),
                "SK", AttributeValue.fromS(sk),
                "metadata", AttributeValue.fromL(mapShoppingListMetadataList(shoppingListMetadataList))
        );
    }

    private List<AttributeValue> mapShoppingListMetadataList(List<ShoppingListInfo> shoppingListMetadataList) {
        return shoppingListMetadataList.stream()
                .map(this::mapShoppingListMetadata)
                .toList();
    }

    private AttributeValue mapShoppingListMetadata(ShoppingListInfo shoppingListMetadata) {
        return AttributeValue.fromM(Map.of(
                "shoppingListName", AttributeValue.fromS(shoppingListMetadata.getShoppingListName()),
                "id", AttributeValue.fromS(shoppingListMetadata.getId()),
                "updatedAt", AttributeValue.fromS(shoppingListMetadata.getUpdatedAt().toString())
        ));
    }
}
