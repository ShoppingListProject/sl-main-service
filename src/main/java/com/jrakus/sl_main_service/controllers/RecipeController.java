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

        List<RecipeInfo> userRecipesMetadata = metadataRepository.getRecipeMetadata(userId);
        List<RecipeInfo> allRecipesMetadata = new ArrayList<>(userRecipesMetadata);

        if (areGlobalRecipesIncluded) {
            List<RecipeInfo> globalRecipesMetadata = metadataRepository.getGlobalRecipeMetadata();
            allRecipesMetadata.addAll(globalRecipesMetadata);
        }

        if (querySearch != null) {
            allRecipesMetadata = allRecipesMetadata.stream().filter(
                    recipe -> recipe.getRecipeName().toLowerCase().contains(querySearch.toLowerCase())
            ).toList();
        }

        List<RecipeInfo> recipesToReturnMetadata = new ArrayList<>();

        for (int i = offset; i < offset + limit && i < allRecipesMetadata.size(); i++) {
            recipesToReturnMetadata.add(
                    allRecipesMetadata.get(i)
            );
        }

        List<String> userRecipeIds = recipesToReturnMetadata.stream()
                .filter(metadata -> !metadata.getIsGlobal())
                .map(RecipeInfo::getId)
                .toList();

        List<String> globalRecipeIds = recipesToReturnMetadata.stream()
                .filter(RecipeInfo::getIsGlobal)
                .map(RecipeInfo::getId)
                .toList();

        List<Recipe> userRecipesToReturn = recipeRepository.getSpecificRecipesForUser(userId, userRecipeIds);
        List<Recipe> globalRecipesToReturn = recipeRepository.getSpecificPublicRecipes(globalRecipeIds);

        List<Recipe> recipesToReturn = new ArrayList<>(userRecipesToReturn);
        recipesToReturn.addAll(globalRecipesToReturn);

        return ResponseEntity.ok(recipesToReturn);
    }

    @Override
    public ResponseEntity<Recipe> createRecipeForUser(String userId, RecipeCreate recipeCreate) {

        String newRecipeId = UUID.randomUUID().toString();
        OffsetDateTime currentDateTime = OffsetDateTime.now();

        Recipe recipe = new Recipe()
                .recipeId(newRecipeId)
                .createdAt(currentDateTime)
                .updatedAt(currentDateTime)
                .name(recipeCreate.getName())
                .items(recipeCreate.getItems());

        List<RecipeInfo> recipeMetadata = metadataRepository.getRecipeMetadata(userId);

        RecipeInfo recipeInfo = new RecipeInfo()
                .recipeName(recipeCreate.getName())
                .id(newRecipeId)
                .isGlobal(false);

        recipeMetadata.add(recipeInfo);

        recipeRepository.saveRecipeForUser(userId, recipe);
        metadataRepository.saveRecipeMetadata(userId, recipeMetadata);

        return ResponseEntity.status(201).body(recipe);
    }

    @Override
    public ResponseEntity<Recipe> removeRecipeForUser(String userId, String recipeId) {

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
    public ResponseEntity<Recipe> updateRecipeForUser(String userId, String recipeId, RecipeCreate recipeUpdate) {

        Optional<Recipe> recipeFromDB = recipeRepository.getUserRecipeById(userId, recipeId);

        if (recipeFromDB.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OffsetDateTime currentDateTime = OffsetDateTime.now();

        Recipe recipe = new Recipe()
                .recipeId(recipeId)
                .name(recipeUpdate.getName())
                .updatedAt(currentDateTime)
                .createdAt(recipeFromDB.get().getCreatedAt())
                .items(recipeUpdate.getItems());

        recipeRepository.saveRecipeForUser(userId, recipe);

        List<RecipeInfo> recipeMetadata = metadataRepository.getRecipeMetadata(userId);

        recipeMetadata = recipeMetadata.stream().map(metadata -> {
            if(metadata.getId().equals(recipeId)) {
                return new RecipeInfo(
                        recipeId,
                        recipeUpdate.getName(),
                        recipeUpdate.getIsGlobal()
                );
            } else {
                return metadata;
            }
        }).toList();

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
            List<RecipeInfo> globalRecipeMetadataList = metadataRepository.getGlobalRecipeMetadata();

            if (querySearch != null) {
                globalRecipeMetadataList = globalRecipeMetadataList.stream().filter(
                        metadata -> metadata.getRecipeName().toLowerCase().contains(querySearch.toLowerCase())
                ).toList();
            }

            numberOfGlobalRecipesToReturn = globalRecipeMetadataList.size();
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
