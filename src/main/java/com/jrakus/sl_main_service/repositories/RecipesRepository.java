package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.Recipe;

import java.util.List;

public interface RecipesRepository {

    List<Recipe> getAllPublicRecipes();

    List<Recipe> getRecipesForUser(String userId);

    void saveRecipeForUser(String userId, Recipe recipe);
}
