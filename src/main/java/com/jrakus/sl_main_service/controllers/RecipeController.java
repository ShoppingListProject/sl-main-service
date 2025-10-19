package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.RecipeRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.Recipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
