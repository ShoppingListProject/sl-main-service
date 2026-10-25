package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.controllers.utils.ShoppingListCreator;
import com.jrakus.sl_main_service.exceptions.specific_exceptions.NotFoundException;
import com.jrakus.sl_main_service.repositories.MetadataRepository;
import com.jrakus.sl_main_service.repositories.RecipeRepository;
import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.*;
import org.openapitools.model.ShoppingList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ShoppingListController implements ShoppingListsApi {

    private final ShoppingListRepository shoppingListRepository;
    private final RecipeRepository recipeRepository;
    private final MetadataRepository metadataRepository;

    private final ShoppingListCreator shoppingListCreator;

    public ShoppingListController(
            ShoppingListRepository shoppingListRepository,
            RecipeRepository recipeRepository,
            MetadataRepository metadataRepository,
            ShoppingListCreator shoppingListCreator
    ) {
        this.shoppingListRepository = shoppingListRepository;
        this.recipeRepository = recipeRepository;
        this.metadataRepository = metadataRepository;

        this.shoppingListCreator = shoppingListCreator;
    }

    @Override
    public ResponseEntity<List<ShoppingList>> getShoppingListsForUser(String userId, Integer offset, Integer limit, String querySearch) {

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        if (querySearch != null) {
            shoppingListMetadataList = shoppingListMetadataList.stream().filter(
                    metadata -> metadata.getShoppingListName().toLowerCase()
                                    .contains(querySearch.toLowerCase())
            ).toList();
        }

        if(offset >= shoppingListMetadataList.size())
            return ResponseEntity.ok(List.of());

        if(offset + limit > shoppingListMetadataList.size())
            limit = shoppingListMetadataList.size() - offset;

        List<ShoppingListInfo> partOfShoppingListMetadataList = shoppingListMetadataList.subList(
                offset, offset + limit
        );

        List<String> shoppingListIdList = partOfShoppingListMetadataList.stream().map(
                ShoppingListInfo::getId
        ).toList();

        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListsForUser(
                userId,
                shoppingListIdList
        );

        return ResponseEntity.ok(shoppingLists);
    }


    @Override
    public ResponseEntity<ShoppingList> createShoppingList(String userId, ShoppingListCreate shoppingListCreate) {

        String newShoppingListId = UUID.randomUUID().toString();
        OffsetDateTime currentDateTime = OffsetDateTime.now();

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(newShoppingListId)
                .name(shoppingListCreate.getName())
                .itemsPerCategory(shoppingListCreate.getItemsPerCategory())
                .createdAt(currentDateTime)
                .updatedAt(currentDateTime);

        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        ShoppingListInfo shoppingListMetadata = new ShoppingListInfo(
                shoppingListCreate.getName(),
                newShoppingListId
        );

        shoppingListMetadataList.add(shoppingListMetadata);
        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> createShoppingListFromRecipes(
            String userId,
            ShoppingListCreateFromRecipes newShoppingListRequest
    ) {

        List<RecipeIdWithNumber> userRecipeArray = newShoppingListRequest.getUserRecipeArray();

        List<String> userRecipeIds = new ArrayList<>(userRecipeArray.stream().map(
                RecipeIdWithNumber::getRecipeId
        ).toList());

        List<Recipe> userRecipes = recipeRepository.getSpecificRecipesForUser(userId, userRecipeIds);

        Set<String> userRecipeIdsFromDB = new HashSet<>(
                userRecipes.stream()
                        .map(Recipe::getRecipeId)
                        .toList()
        );

        if (!userRecipeIdsFromDB.containsAll(userRecipeIds)) {

            userRecipeIds.removeAll(userRecipeIdsFromDB);

            throw new NotFoundException(
                    String.format("Recipes with ids: %s couldn't be found", userRecipeIds),
                    Code.MANY_RECIPES_NOT_FOUND
            );
        }

        List<RecipeIdWithNumber> publicRecipeArray = newShoppingListRequest.getPublicRecipeArray();

        List<String> publicRecipeIds = new ArrayList<>(publicRecipeArray.stream().map(
                RecipeIdWithNumber::getRecipeId
        ).toList());

        List<Recipe> publicRecipes = recipeRepository.getSpecificPublicRecipes(publicRecipeIds);

        Set<String> publicRecipeIdsFromDB = new HashSet<>(
                publicRecipes.stream()
                        .map(Recipe::getRecipeId)
                        .toList()
        );

        if (!publicRecipeIdsFromDB.containsAll(publicRecipeIds)) {

            publicRecipeIds.removeAll(publicRecipeIdsFromDB);

            throw new NotFoundException(
                    String.format("Recipes with ids: %s couldn't be found", publicRecipeIds),
                    Code.MANY_RECIPES_NOT_FOUND
            );
        }

        List<Recipe> allPreparedRecipes = shoppingListCreator.prepareRecipes(
          userRecipes, publicRecipes, userRecipeArray, publicRecipeArray
        );

        String newShoppingListName = newShoppingListRequest.getName();

        ShoppingList shoppingList = shoppingListCreator.createShoppingList(newShoppingListName, allPreparedRecipes);
        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        // TODO:
        //  Handle situation when the shopping list metadata has not been created yet.
        //  It happens when user creates his first shopping list
        //  NOTE: we are going to do that when customer is registering for the first time

        ShoppingListInfo shoppingListMetadata = new ShoppingListInfo(
                newShoppingListName,
                shoppingList.getShoppingListId()
        );

        shoppingListMetadataList.add(shoppingListMetadata);
        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> deleteShoppingList(String userId, String shoppingListId) {

        Optional<ShoppingList> shoppingListOptional =
                shoppingListRepository.getUserShoppingListById(userId, shoppingListId);

        if(shoppingListOptional.isEmpty()) {
            throw new NotFoundException(
                    String.format("Shopping list with ID %s couldn't be found", shoppingListId),
                    Code.SHOPPING_LIST_NOT_FOUND
            );
        }

        shoppingListRepository.deleteShoppingListForUser(userId, shoppingListId);

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        List<ShoppingListInfo> updatedShoppingListMetadataList = shoppingListMetadataList.stream().filter(
                shoppingListInfo ->
                        !shoppingListInfo.getId().equals(shoppingListOptional.get().getShoppingListId())
        ).toList();

        metadataRepository.saveShoppingListMetadata(userId, updatedShoppingListMetadataList);

        return ResponseEntity.ok(shoppingListOptional.get());
    }

    @Override
    public ResponseEntity<ShoppingList> updateShoppingList(
            String userId, String shoppingListId, ShoppingListCreate shoppingListUpdate
    ) {

        Optional<ShoppingList> shoppingListFromDB = shoppingListRepository.getUserShoppingListById(userId, shoppingListId);

        if (shoppingListFromDB.isEmpty()) {
            throw new NotFoundException(
                    String.format("Shopping list with ID %s couldn't be found", shoppingListId),
                    Code.SHOPPING_LIST_NOT_FOUND
            );
        }

        OffsetDateTime currentDateTime = OffsetDateTime.now();

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(shoppingListId)
                .name(shoppingListUpdate.getName())
                .updatedAt(currentDateTime)
                .createdAt(shoppingListFromDB.get().getCreatedAt())
                .itemsPerCategory(shoppingListUpdate.getItemsPerCategory());


        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        List<ShoppingListInfo> shoppingListMetadata = metadataRepository.getShoppingListMetadata(userId);

        ShoppingListInfo updatedShoppingInfo = new ShoppingListInfo(
                shoppingList.getName(),
                shoppingListId
        );

        List<ShoppingListInfo> updatedShoppingListMetadata = shoppingListMetadata.stream().map(metadata -> {
            if(metadata.getId().equals(shoppingListId)) {
                return updatedShoppingInfo;
            } else {
                return metadata;
            }
        }).toList();

        metadataRepository.saveShoppingListMetadata(userId, updatedShoppingListMetadata);

        return ResponseEntity.ok(shoppingList);
    }

    @Override
    public ResponseEntity<NumberOfPages> getPagesForShoppingLists(String userId, String itemsPerPage, String querySearch) {

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        if (querySearch != null) {
            shoppingListMetadataList = shoppingListMetadataList.stream().filter(
                    metadata -> metadata.getShoppingListName().toLowerCase().contains(
                            querySearch.toLowerCase()
                    )
            ).toList();
        }

        int numberOfPages =  Math.ceilDiv(shoppingListMetadataList.size(), Integer.parseInt(itemsPerPage));
        NumberOfPages numberOfPagesObject = new NumberOfPages(numberOfPages);

        return ResponseEntity.ok(numberOfPagesObject);
    }

    @Override
    public ResponseEntity<List<ShoppingListInfo>> getMetadataForShoppingLists(String userId) {
        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        return ResponseEntity.ok(shoppingListMetadataList);
    }
}
