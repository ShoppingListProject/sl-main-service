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

    public Recipe fromDynamoDB(Map<String, AttributeValue> item) {

        Recipe recipe = new Recipe();

        recipe.setName(item.get("name").s());
        recipe.setCreatedAt(OffsetDateTime.parse(item.get("createdAt").s()));
        recipe.setUpdatedAt(OffsetDateTime.parse(item.get("updatedAt").s()));
        recipe.setItems(mapToRecipeItems(item.get("items").l()));

        return recipe;
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
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "name", AttributeValue.builder().s(recipe.getName()).build(),
                "createdAt", AttributeValue.builder().s(recipe.getCreatedAt().toString()).build(),
                "updatedAt", AttributeValue.builder().s(recipe.getUpdatedAt().toString()).build(),
                "items", AttributeValue.builder().l(mapFromRecipeItems(recipe.getItems())).build()
        );
    }

    private List<AttributeValue> mapFromRecipeItems(List<RecipeItem> items) {
        return items.stream()
                .map(this::mapFromRecipeItem)
                .toList();
    }

    private AttributeValue mapFromRecipeItem(RecipeItem item) {
        return AttributeValue.builder().m(Map.of(
                "category", AttributeValue.builder().s(item.getCategory()).build(),
                "name", AttributeValue.builder().s(item.getName()).build(),
                "quantity", AttributeValue.builder().n(String.valueOf(item.getQuantity())).build(),
                "unit", AttributeValue.builder().s(item.getUnit()).build()
        )).build();
    }
}
