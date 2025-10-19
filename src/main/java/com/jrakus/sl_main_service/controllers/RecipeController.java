package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeBase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RecipeController implements RecipesApi {

    private final RecipeRepository recipeRepository;

    public RecipeController(RecipeRepository recipesRepository) {
        this.recipeRepository = recipesRepository;
    }

    @Override
    public ResponseEntity<List<Recipe>> getRecipesForUser(String userId) {

        List<Recipe> recipes = recipeRepository.getRecipesForUser(userId);

        return ResponseEntity.ok(recipes);
    }

    @Override
    public ResponseEntity<List<Recipe>> getPublicRecipes() {

        List<Recipe> recipes = recipeRepository.getAllPublicRecipes();

        return ResponseEntity.ok(recipes);
    }

    @Override
    public ResponseEntity<Recipe> createRecipesForUser(String userId, RecipeBase recipeBase) {

        String newRecipeId = UUID.randomUUID().toString();

        Recipe recipe = new Recipe()
                .recipeId(newRecipeId)
                .createdAt(recipeBase.getCreatedAt())
                .updatedAt(recipeBase.getUpdatedAt())
                .name(recipeBase.getName())
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);
        return ResponseEntity.status(201).body(recipe);
    }

    @Override
    public ResponseEntity<Recipe> removeRecipesForUser(String userId, String recipeId) {

        // TODO
        // 1) Check if the element already exists
        // 2) Return the deleted item

        recipeRepository.deleteRecipeForUser(userId, recipeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Recipe> updateRecipesForUser(String userId, String recipeId, RecipeBase recipeBase) {

        // TODO
        // Check if the element already exists

        Recipe recipe = new Recipe()
                .recipeId(recipeId)
                .name(recipeBase.getName())
                .updatedAt(recipeBase.getUpdatedAt())
                .createdAt(recipeBase.getCreatedAt())
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);

        return ResponseEntity.ok(recipe);
    }
}
