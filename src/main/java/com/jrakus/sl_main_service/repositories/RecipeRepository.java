package com.jrakus.sl_main_service.repositories;

import org.openapitools.model.Recipe;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    List<Recipe> getAllPublicRecipes();

    List<Recipe> getSpecificPublicRecipes(List<String> recipeIds);

    List<Recipe> getRecipesForUser(String userId);

    List<Recipe> getSpecificRecipesForUser(String userId, List<String> recipeIds);

    Optional<Recipe> getUserRecipeById(String userId, String recipeId);

    void saveRecipeForUser(String userId, Recipe recipe);

    void deleteRecipeForUser(String userId, String recipeId);
}
