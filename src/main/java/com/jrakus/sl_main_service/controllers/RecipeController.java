package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.Recipe;
import org.openapitools.model.RecipeCreate;
import org.openapitools.model.RecipeUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    public ResponseEntity<List<Recipe>> getRecipesForUser(String userId, Integer offset, Integer limit, Boolean areGlobalRecipesIncluded, String querySearch) {

        List<Recipe> userRecipes = recipeRepository.getRecipesForUser(userId);
        List<Recipe> allRecipes = new ArrayList<>(userRecipes);

        if(areGlobalRecipesIncluded) {
            List<Recipe> publicRecipes = recipeRepository.getAllPublicRecipes();
            allRecipes.addAll(publicRecipes);
        }

        return ResponseEntity.ok(allRecipes);
    }

    @Override
    public ResponseEntity<Recipe> createRecipeForUser(String userId, RecipeCreate recipeBase) {

        String newRecipeId = UUID.randomUUID().toString();
        OffsetDateTime currentDateTime = OffsetDateTime.now();

        Recipe recipe = new Recipe()
                .recipeId(newRecipeId)
                .createdAt(currentDateTime)
                .updatedAt(currentDateTime)
                .name(recipeBase.getName())
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);
        return ResponseEntity.status(201).body(recipe);
    }

    @Override
    public ResponseEntity<Recipe> removeRecipeForUser(String userId, String recipeId) {

        // TODO
        // 1) Add error message to body when 404 happens

        Optional<Recipe> recipeOptional = recipeRepository.getUserRecipeById(userId, recipeId);

        if(recipeOptional.isEmpty())
            return ResponseEntity.notFound().build();

        recipeRepository.deleteRecipeForUser(userId, recipeId);
        return ResponseEntity.ok(recipeOptional.get());
    }

    @Override
    public ResponseEntity<Recipe> updateRecipeForUser(String userId, String recipeId, RecipeUpdate recipeBase) {

        // TODO
        // Check if the element already exists
        // Do not update createAt - use the previous value taken from DB

        OffsetDateTime currentDateTime = OffsetDateTime.now();

        Recipe recipe = new Recipe()
                .recipeId(recipeId)
                .name(recipeBase.getName())
                .updatedAt(currentDateTime)
                .createdAt(currentDateTime)
                .items(recipeBase.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);

        return ResponseEntity.ok(recipe);
    }
}
