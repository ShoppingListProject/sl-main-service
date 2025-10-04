package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.RecipesRepository;
import org.openapitools.model.Recipe;

import java.util.List;

public class RecipesRepositoryDynamoDB implements RecipesRepository {

    @Override
    public List<Recipe> getAllPublicRecipes() {
        return List.of();
    }
}
