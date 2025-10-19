package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.Recipe;

import java.util.List;

public interface RecipeRepository {

    List<Recipe> getAllPublicRecipes();

    List<Recipe> getSpecificPublicRecipes(List<String> recipeIds);

    List<Recipe> getRecipesForUser(String userId);

    List<Recipe> getSpecificRecipesForUser(String userId, List<String> recipeIds);

    void saveRecipeForUser(String userId, Recipe recipe);
}
