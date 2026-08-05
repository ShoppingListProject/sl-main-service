package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.repositories.MetadataRepository;
import com.jrakus.sl_main_service.repositories.RecipeRepository;
import org.openapitools.api.RecipesApi;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
public class RecipeController implements RecipesApi {

    private final RecipeRepository recipeRepository;
    private final MetadataRepository metadataRepository;

    public RecipeController(RecipeRepository recipesRepository, MetadataRepository metadataRepository) {
        this.recipeRepository = recipesRepository;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public ResponseEntity<List<Recipe>> getRecipesForUser(String userId, Integer offset, Integer limit, Boolean areGlobalRecipesIncluded, String querySearch) {

        // TODO:
        //  We could use recipeMetadata that stores info about all user recipes instead of getting all recipes
        //  (it could be slightly faster). However we do not have metadata for Global recipes so probably we should add
        //  it first.

        List<Recipe> userRecipes = recipeRepository.getRecipesForUser(userId);
        List<Recipe> allRecipes = new ArrayList<>(userRecipes);

        if (areGlobalRecipesIncluded) {
            List<Recipe> publicRecipes = recipeRepository.getAllPublicRecipes();
            allRecipes.addAll(publicRecipes);
        }

        if (querySearch != null) {
            allRecipes = allRecipes.stream().filter(
                    recipe -> recipe.getName().toLowerCase(Locale.ROOT).equals(querySearch.toLowerCase())
            ).toList();
        }

        List<Recipe> recipesToReturn = new ArrayList<>();

        for (int i = offset; i < offset + limit && i < allRecipes.size(); i++) {
            recipesToReturn.add(
                    allRecipes.get(i)
            );
        }

        return ResponseEntity.ok(recipesToReturn);
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

        List<RecipeInfo> recipeMetadata = metadataRepository.getRecipeMetadata(userId);

        RecipeInfo recipeInfo = new RecipeInfo()
                .recipeName(recipeBase.getName())
                .id(newRecipeId)
                .updatedAt(currentDateTime);

        recipeMetadata.add(recipeInfo);

        recipeRepository.saveRecipeForUser(userId, recipe);
        metadataRepository.saveRecipeMetadata(userId, recipeMetadata);

        return ResponseEntity.status(201).body(recipe);
    }

    @Override
    public ResponseEntity<Recipe> removeRecipeForUser(String userId, String recipeId) {

        // TODO
        // 1) Add error message to body when 404 happens

        Optional<Recipe> recipeOptional = recipeRepository.getUserRecipeById(userId, recipeId);

        if(recipeOptional.isEmpty())
            return ResponseEntity.notFound().build();

        List<RecipeInfo> recipeMetadata = metadataRepository.getRecipeMetadata(userId);

        recipeMetadata = recipeMetadata.stream().filter(
                recipeInfo -> !recipeInfo.getId().equals(recipeOptional.get().getRecipeId())
        ).toList();

        recipeRepository.deleteRecipeForUser(userId, recipeId);
        metadataRepository.saveRecipeMetadata(userId, recipeMetadata);

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

        List<RecipeInfo> recipeMetadata = metadataRepository.getRecipeMetadata(userId);

        RecipeInfo recipeInfo = new RecipeInfo()
                .recipeName(recipeBase.getName())
                .id(recipeId)
                .updatedAt(currentDateTime);

        recipeMetadata.add(recipeInfo);

        recipeRepository.saveRecipeForUser(userId, recipe);
        metadataRepository.saveRecipeMetadata(userId, recipeMetadata);

        return ResponseEntity.ok(recipe);
    }

    @Override
    public ResponseEntity<NumberOfPages> getPagesForRecipes(
            String userId,
            String itemsPerPage,
            String querySearch,
            Boolean areGlobalRecipesIncluded
    ) {

        List<RecipeInfo> recipeMetadataList = metadataRepository.getRecipeMetadata(userId);

        if (querySearch != null) {
            recipeMetadataList = recipeMetadataList.stream().filter(
                    metadata -> metadata.getRecipeName().toLowerCase().contains(
                            querySearch.toLowerCase()
                    )
            ).toList();
        }

        int numberOfGlobalRecipesToReturn = 0;

        if (areGlobalRecipesIncluded) {
            List<Recipe> globalRecipes = recipeRepository.getAllPublicRecipes();

            if (querySearch != null) {
                globalRecipes = globalRecipes.stream().filter(
                        recipe -> recipe.getName().toLowerCase().contains(querySearch.toLowerCase())
                ).toList();
            }

            numberOfGlobalRecipesToReturn = globalRecipes.size();
        }

        int numberOfPages =  Math.ceilDiv(recipeMetadataList.size() + numberOfGlobalRecipesToReturn, Integer.parseInt(itemsPerPage));
        NumberOfPages numberOfPagesObject = new NumberOfPages(numberOfPages);

        return ResponseEntity.ok(numberOfPagesObject);

    }

    @Override
    public ResponseEntity<List<RecipeInfo>> getMetadataForRecipes(String userId) {
        List<RecipeInfo> recipeMetadataList = metadataRepository.getRecipeMetadata(userId);

        return ResponseEntity.ok(recipeMetadataList);
    }
}
