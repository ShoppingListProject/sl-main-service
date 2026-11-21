package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipesMetadataMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListsMetadataMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.model.RecipeInfo;
import org.openapitools.model.ShoppingListInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataRepositoryDynamoDBIntegrationTests extends DynamoDBIntegrationTestBase {

    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";

    private final RecipesMetadataMapper recipesMetadataMapper = new RecipesMetadataMapper();
    private final MetadataRepositoryDynamoDB metadataRepository = new MetadataRepositoryDynamoDB(
            dynamoDBQueryHelper,
            new ShoppingListsMetadataMapper(),
            recipesMetadataMapper
    );

    // ==============================
    // ===== SHOPPING LIST INFO =====
    // ==============================

    @Test
    void shouldSaveAndReadShoppingListMetadata() {

        // given
        List<ShoppingListInfo> metadata = List.of(
                new ShoppingListInfo("weekend", "1"),
                new ShoppingListInfo("party", "2")
        );

        // when
        metadataRepository.saveShoppingListMetadata(USER_ID, metadata);

        // then
        assertEquals(metadata, metadataRepository.getShoppingListMetadata(USER_ID));
    }

    @Test
    void shouldSaveAndReadEmptyShoppingListMetadata() {

        // when
        metadataRepository.saveShoppingListMetadata(USER_ID, List.of());

        // then
        assertEquals(List.of(), metadataRepository.getShoppingListMetadata(USER_ID));
    }

    @Test
    void shouldReplaceWholeShoppingListMetadataOnSave() {

        // given
        metadataRepository.saveShoppingListMetadata(USER_ID, List.of(
                new ShoppingListInfo("weekend", "1"),
                new ShoppingListInfo("party", "2")
        ));
        List<ShoppingListInfo> updated = List.of(new ShoppingListInfo("party", "2"));

        // when
        metadataRepository.saveShoppingListMetadata(USER_ID, updated);

        // then
        assertEquals(updated, metadataRepository.getShoppingListMetadata(USER_ID));
    }

    @Test
    void shouldNotReturnShoppingListMetadataOfOtherUser() {

        // given
        metadataRepository.saveShoppingListMetadata(USER_ID, List.of(new ShoppingListInfo("weekend", "1")));
        metadataRepository.saveShoppingListMetadata(OTHER_USER_ID, List.of(new ShoppingListInfo("party", "2")));

        // when
        List<ShoppingListInfo> result = metadataRepository.getShoppingListMetadata(USER_ID);

        // then
        assertEquals(List.of(new ShoppingListInfo("weekend", "1")), result);
    }

    @Test
    void shouldReturnMutableEmptyShoppingListMetadataWhenMissing() {

        // when
        List<ShoppingListInfo> result = metadataRepository.getShoppingListMetadata(USER_ID);

        // then
        assertEquals(List.of(), result);
        assertDoesNotThrow(() -> result.add(new ShoppingListInfo("weekend", "1")));
    }

    // ===========================
    // ===== RECIPE INFO =========
    // ===========================

    @Test
    void shouldSaveAndReadRecipeMetadata() {

        // given
        List<RecipeInfo> metadata = List.of(
                new RecipeInfo("pancakes", "1", false),
                new RecipeInfo("soup", "2", true)
        );

        // when
        metadataRepository.saveRecipeMetadata(USER_ID, metadata);

        // then
        assertEquals(metadata, metadataRepository.getRecipeMetadata(USER_ID));
    }

    @Test
    void shouldSaveAndReadEmptyRecipeMetadata() {

        // when
        metadataRepository.saveRecipeMetadata(USER_ID, List.of());

        // then
        assertEquals(List.of(), metadataRepository.getRecipeMetadata(USER_ID));
    }

    @Test
    void shouldReplaceWholeRecipeMetadataOnSave() {

        // given
        metadataRepository.saveRecipeMetadata(USER_ID, List.of(
                new RecipeInfo("pancakes", "1", false),
                new RecipeInfo("soup", "2", false)
        ));
        List<RecipeInfo> updated = List.of(new RecipeInfo("soup", "2", false));

        // when
        metadataRepository.saveRecipeMetadata(USER_ID, updated);

        // then
        assertEquals(updated, metadataRepository.getRecipeMetadata(USER_ID));
    }

    @Test
    void shouldNotReturnRecipeMetadataOfOtherUser() {

        // given
        metadataRepository.saveRecipeMetadata(USER_ID, List.of(new RecipeInfo("pancakes", "1", false)));
        metadataRepository.saveRecipeMetadata(OTHER_USER_ID, List.of(new RecipeInfo("soup", "2", false)));

        // when
        List<RecipeInfo> result = metadataRepository.getRecipeMetadata(USER_ID);

        // then
        assertEquals(List.of(new RecipeInfo("pancakes", "1", false)), result);
    }

    @Test
    void shouldReturnMutableEmptyRecipeMetadataWhenMissing() {

        // when
        List<RecipeInfo> result = metadataRepository.getRecipeMetadata(USER_ID);

        // then
        assertEquals(List.of(), result);
        assertDoesNotThrow(() -> result.add(new RecipeInfo("pancakes", "1", false)));
    }

    @Test
    void shouldKeepShoppingListAndRecipeMetadataOfUserSeparate() {

        // given
        List<ShoppingListInfo> shoppingLists = List.of(new ShoppingListInfo("weekend", "1"));
        List<RecipeInfo> recipes = List.of(new RecipeInfo("pancakes", "1", false));

        // when
        metadataRepository.saveShoppingListMetadata(USER_ID, shoppingLists);
        metadataRepository.saveRecipeMetadata(USER_ID, recipes);

        // then
        assertEquals(shoppingLists, metadataRepository.getShoppingListMetadata(USER_ID));
        assertEquals(recipes, metadataRepository.getRecipeMetadata(USER_ID));
    }

    // ===========================
    // ===== GLOBAL RECIPES ======
    // ===========================

    @Test
    void shouldReadGlobalRecipeMetadata() {

        // given
        List<RecipeInfo> globalMetadata = List.of(
                new RecipeInfo("pancakes", "1", true),
                new RecipeInfo("soup", "2", true)
        );
        saveGlobalRecipeMetadata(globalMetadata);

        // when
        List<RecipeInfo> result = metadataRepository.getGlobalRecipeMetadata();

        // then
        assertEquals(globalMetadata, result);
    }

    @Test
    void shouldReturnEmptyGlobalRecipeMetadataWhenMissing() {

        // when
        List<RecipeInfo> result = metadataRepository.getGlobalRecipeMetadata();

        // then
        assertEquals(List.of(), result);
    }

    @Test
    void shouldNotMixGlobalAndUserRecipeMetadata() {

        // given
        List<RecipeInfo> globalMetadata = List.of(new RecipeInfo("pancakes", "1", true));
        List<RecipeInfo> userMetadata = List.of(new RecipeInfo("soup", "2", false));
        saveGlobalRecipeMetadata(globalMetadata);
        metadataRepository.saveRecipeMetadata(USER_ID, userMetadata);

        // when / then
        assertEquals(globalMetadata, metadataRepository.getGlobalRecipeMetadata());
        assertEquals(userMetadata, metadataRepository.getRecipeMetadata(USER_ID));
    }

    // ===================
    // ===== HELPERS =====
    // ===================

    // the repository has no write method for global metadata, so it is inserted directly.
    // it can be removed in the future when we add the method.
    private void saveGlobalRecipeMetadata(List<RecipeInfo> metadata) {
        var item = recipesMetadataMapper.toDynamoDBItem("GLOBAL#RECIPES", "METADATA", metadata);
        dynamoDBQueryHelper.saveSingleItem(item);
    }
}
