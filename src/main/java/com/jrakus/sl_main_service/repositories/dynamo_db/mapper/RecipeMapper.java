package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeItem;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {

    public Recipe fromDynamoDB(Map<String, AttributeValue> item) {

        Recipe recipe = new Recipe();

        recipe.setName(item.get("name").s());
        recipe.setCreatedAt(item.get("createdAt").s());
        recipe.setUpdatedAt(item.get("updatedAt").s());
        recipe.setItems(mapRecipeItems(item.get("items").l()));

        return recipe;
    }

    private List<RecipeItem> mapRecipeItems(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapRecipeItem)
                .toList();
    }

    private RecipeItem mapRecipeItem(Map<String, AttributeValue> itemMap) {
        return new RecipeItem()
                .category(itemMap.get("category").s())
                .name(itemMap.get("name").s())
                .quantity(Integer.parseInt(itemMap.get("quantity").n()))
                .unit(itemMap.get("unit").s());
    }
}
