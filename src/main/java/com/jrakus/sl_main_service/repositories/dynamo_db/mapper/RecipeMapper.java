 package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Component
public class RecipeMapper {

    // ======================
    // ======== READ ========
    // ======================

    public Recipe fromDynamoDB(Map<String, AttributeValue> item, boolean isPublic) {

        String recipeId = item.get("SK").s().split("#")[1];

        return new Recipe()
                .recipeId(recipeId)
                .name(item.get("name").s())
                .createdAt(OffsetDateTime.parse(item.get("createdAt").s()))
                .updatedAt(OffsetDateTime.parse(item.get("updatedAt").s()))
                .items(mapToRecipeItems(item.get("items").l()))
                .isGlobal(isPublic);
    }

    private List<RecipeItem> mapToRecipeItems(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToRecipeItem)
                .toList();
    }

    private RecipeItem mapToRecipeItem(Map<String, AttributeValue> itemMap) {
        return new RecipeItem()
                .category(itemMap.get("category").s())
                .name(itemMap.get("name").s())
                .quantity(Float.valueOf(itemMap.get("quantity").n()))
                .unit(itemMap.get("unit").s());
    }

    // =======================
    // ======== WRITE ========
    // =======================

    public Map<String, AttributeValue> toDynamoDBItem(
            String pk,
            String sk,
            Recipe recipe
    ) {
        return Map.of(
                "PK", AttributeValue.fromS(pk),
                "SK", AttributeValue.fromS(sk),
                "name", AttributeValue.fromS(recipe.getName()),
                "createdAt", AttributeValue.fromS(recipe.getCreatedAt().toString()),
                "updatedAt", AttributeValue.fromS(recipe.getUpdatedAt().toString()),
                "items", AttributeValue.fromL(mapFromRecipeItems(recipe.getItems()))
        );
    }

    private List<AttributeValue> mapFromRecipeItems(List<RecipeItem> items) {
        return items.stream()
                .map(this::mapFromRecipeItem)
                .toList();
    }

    private AttributeValue mapFromRecipeItem(RecipeItem item) {
        return AttributeValue.fromM(Map.of(
                "category", AttributeValue.fromS(item.getCategory()),
                "name", AttributeValue.fromS(item.getName()),
                "quantity", AttributeValue.fromN(String.valueOf(item.getQuantity())),
                "unit", AttributeValue.fromS(item.getUnit())
        ));
    }
}
