package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.RecipeInfo;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecipesMetadataMapper {

    // ======================
    // ======== READ ========
    // ======================

    public List<RecipeInfo> fromDynamoDBItem(Map<String, AttributeValue> item) {
        return mapToRecipeMetadataList(item.get("metadata").l());
    }

    private List<RecipeInfo> mapToRecipeMetadataList(List<AttributeValue> items) {
        return items.stream()
                .map(AttributeValue::m)
                .map(this::mapToRecipeMetadata)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private RecipeInfo mapToRecipeMetadata(Map<String, AttributeValue> item) {

        String recipeName = item.get("recipeName").s();
        String id = item.get("id").s();

        return new RecipeInfo(recipeName, id);
    }

    // =======================
    // ======== WRITE ========
    // =======================

    public Map<String, AttributeValue> toDynamoDBItem(
            String pk,
            String sk,
            List<RecipeInfo> recipeMetadataList
    ) {
        return Map.of(
                "PK", AttributeValue.fromS(pk),
                "SK", AttributeValue.fromS(sk),
                "metadata", AttributeValue.fromL((mapRecipeMetadataList(recipeMetadataList)))
        );
    }

    private List<AttributeValue> mapRecipeMetadataList(List<RecipeInfo> recipeMetadataList) {
        return recipeMetadataList.stream()
                .map(this::mapRecipeMetadata)
                .toList();
    }

    private AttributeValue mapRecipeMetadata(RecipeInfo recipeMetadata) {

        AttributeValue recipeName = AttributeValue.fromS(recipeMetadata.getRecipeName());
        AttributeValue id = AttributeValue.fromS(recipeMetadata.getId());

        return AttributeValue.fromM(
                Map.of(
                        "recipeName", recipeName,
                        "id", id
                )
        );
    }
}
