package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.openapitools.model.RecipeInfo;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
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
        OffsetDateTime updatedAt = OffsetDateTime.parse(item.get("updatedAt").s());

        return new RecipeInfo(recipeName, id, updatedAt);
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
                "PK", AttributeValue.builder().s(pk).build(),
                "SK", AttributeValue.builder().s(sk).build(),
                "metadata", AttributeValue.builder().l(mapRecipeMetadataList(recipeMetadataList)).build()
        );
    }

    private List<AttributeValue> mapRecipeMetadataList(List<RecipeInfo> recipeMetadataList) {
        return recipeMetadataList.stream()
                .map(this::mapRecipeMetadata)
                .toList();
    }

    private AttributeValue mapRecipeMetadata(RecipeInfo recipeMetadata) {

        AttributeValue recipeName = AttributeValue.builder().s(
                recipeMetadata.getRecipeName()
        ).build();

        AttributeValue id = AttributeValue.builder().s(
                recipeMetadata.getId()
        ).build();

        AttributeValue updatedAt = AttributeValue.builder().s(
                recipeMetadata.getUpdatedAt().toString()
        ).build();

        return AttributeValue.builder()
                .m(Map.of(
                        "recipeName", recipeName,
                        "id", id,
                        "updatedAt", updatedAt
                ))
                .build();
    }
}
