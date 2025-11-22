package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeCreate;
import org.openapitools.model.RecipeUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<Recipe> createRecipesForUser(String userId, RecipeCreate recipeBase) {

        String newRecipeId = UUID.randomUUID().toString();

        Recipe recipe = new Recipe()
                .recipeId(newRecipeId)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .name(recipeBase.getName())
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);
        return ResponseEntity.status(201).body(recipe);
    }

    @Override
    public ResponseEntity<Recipe> removeRecipesForUser(String userId, String recipeId) {

        // TODO
        // 1) Add error message to body when 404 happens

        Optional<Recipe> recipeOptional = recipeRepository.getUserRecipeById(userId, recipeId);

        if(recipeOptional.isEmpty())
            return ResponseEntity.notFound().build();

        recipeRepository.deleteRecipeForUser(userId, recipeId);
        return ResponseEntity.ok(recipeOptional.get());
    }

    @Override
    public ResponseEntity<Recipe> updateRecipesForUser(String userId, String recipeId, RecipeUpdate recipeBase) {

        // TODO
        // Check if the element already exists
        // Do not update createAt - use the previous value taken from DB

        Recipe recipe = new Recipe()
                .recipeId(recipeId)
                .name(recipeBase.getName())
                .updatedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);

        return ResponseEntity.ok(recipe);
    }
}
