package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.RecipesRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.Recipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecipeController implements RecipesApi {

    private final RecipesRepository recipesRepository;

    public RecipeController(RecipesRepository recipesRepository) {
        this.recipesRepository = recipesRepository;
    }

    @Override
    public ResponseEntity<List<Recipe>> getRecipesForUser(String userId) {

        List<Recipe> recipes = recipesRepository.getRecipesForUser(userId);

        return ResponseEntity.ok(recipes);
    }

    @Override
    public ResponseEntity<List<Recipe>> getPublicRecipes() {

        List<Recipe> recipes = recipesRepository.getAllPublicRecipes();

        return ResponseEntity.ok(recipes);
    }
}
