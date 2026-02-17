package com.jrakus.sl_main_service.controllers;

import com.jrakus.sl_main_service.controllers.utils.ShoppingListCreator;
import com.jrakus.sl_main_service.repositories.MetadataRepository;
import com.jrakus.sl_main_service.repositories.RecipeRepository;
import com.jrakus.sl_main_service.repositories.ShoppingListRepository;
import org.openapitools.api.ShoppingListsApi;
import org.openapitools.model.*;
import org.openapitools.model.ShoppingList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.*;

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

        // TODO: verify the passed parameters

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        if (querySearch != null) {
            shoppingListMetadataList = shoppingListMetadataList.stream().filter(
                    metadata -> metadata.getShoppingListName().toLowerCase()
                                    .contains(querySearch.toLowerCase())
            ).toList();
        }

        if(offset >= shoppingListMetadataList.size())
            return ResponseEntity.ok(List.of());

        if(limit > shoppingListMetadataList.size())
            limit = shoppingListMetadataList.size();

        List<ShoppingListInfo> partOfShoppingListMetadataList = shoppingListMetadataList.subList(offset, limit);
        List<ShoppingListInfo> sortedShoppingListMetadata = partOfShoppingListMetadataList.stream().sorted(
                Comparator.comparing(ShoppingListInfo::getUpdatedAt)
        ).toList();

        List<ShoppingList> shoppingLists = shoppingListRepository.getShoppingListsForUser(
                userId,
                sortedShoppingListMetadata.getFirst().getUpdatedAt(),
                sortedShoppingListMetadata.getLast().getUpdatedAt()
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

        // TODO:
        //  Handle situation when the shopping list metadata has not been created yet.
        //  It happens when user creates his first shopping list

        ShoppingListInfo shoppingListMetadata = new ShoppingListInfo(
                shoppingListCreate.getName(),
                newShoppingListId,
                currentDateTime
        );

        shoppingListMetadataList.add(shoppingListMetadata);
        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> createShoppingListFromRecipes(String userId, ShoppingListCreateFromRecipes newShoppingListRequest) {

        List<RecipeIdWithNumber> userRecipeArray = newShoppingListRequest.getUserRecipeArray();
        List<RecipeIdWithNumber> publicRecipeArray = newShoppingListRequest.getPublicRecipeArray();

        List<String> userRecipeIds = userRecipeArray.stream().map(
                RecipeIdWithNumber::getRecipeId
        ).toList();

        List<String> publicRecipeIds = publicRecipeArray.stream().map(
                RecipeIdWithNumber::getRecipeId
        ).toList();

        List<Recipe> userRecipes = recipeRepository.getSpecificRecipesForUser(userId, userRecipeIds);
        List<Recipe> publicRecipes = recipeRepository.getSpecificPublicRecipes(publicRecipeIds);

        // TODO
        // 1) Throw an error if some recipe couldn't be found.
        // 2 ) Move the code responsible for preparing Recipes to separate class

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

        ShoppingListInfo shoppingListMetadata = new ShoppingListInfo(
                newShoppingListName,
                shoppingList.getShoppingListId(),
                shoppingList.getUpdatedAt()
        );

        shoppingListMetadataList.add(shoppingListMetadata);
        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.status(201).body(shoppingList);
    }

    @Override
    public ResponseEntity<ShoppingList> deleteShoppingList(String userId, String shoppingListId) {

        // TODO
        // 1) Add error message to body when 404 happens

        Optional<ShoppingList> shoppingListOptional =
                shoppingListRepository.getUserShoppingListById(userId, shoppingListId);

        if(shoppingListOptional.isEmpty())
            return ResponseEntity.notFound().build();

        shoppingListRepository.deleteShoppingListForUser(userId, shoppingListId);

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);
        Optional<ShoppingListInfo> deletedShoppingListMetadataOptional = shoppingListMetadataList.stream()
                .filter(m -> m.getId().equals(shoppingListId))
                .findAny();

        deletedShoppingListMetadataOptional.ifPresent(shoppingListMetadataList::remove);

        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.ok(shoppingListOptional.get());
    }

    @Override
    public ResponseEntity<ShoppingList> updateShoppingList(
            String userId, String shoppingListId, ShoppingListUpdate shoppingListBase
    ) {

        // TODO
        // Check if the element already exists
        // Do not update createdAt field - Reuse the value taken from DB

        OffsetDateTime currentDateTime = OffsetDateTime.now();

        ShoppingList shoppingList = new ShoppingList()
                .shoppingListId(shoppingListId)
                .name(shoppingListBase.getName())
                .updatedAt(currentDateTime)
                .createdAt(currentDateTime)
                .itemsPerCategory(shoppingListBase.getItemsPerCategory());


        shoppingListRepository.saveShoppingListForUser(userId, shoppingList);

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        ShoppingListInfo updatedShoppingListMetadata = new ShoppingListInfo(
                shoppingList.getName(),
                shoppingList.getShoppingListId(),
                currentDateTime
        );

        int index = 0;
        for (int i = 0; i < shoppingListMetadataList.size(); i++) {

            String metadataId = shoppingListMetadataList.get(i).getId();

            if (metadataId.equals(shoppingListId)) {
                index = i;
                break;
            }
        }

        shoppingListMetadataList.add(index, updatedShoppingListMetadata);
        metadataRepository.saveShoppingListMetadata(userId, shoppingListMetadataList);

        return ResponseEntity.ok(shoppingList);
    }

    @Override
    public ResponseEntity<NumberOfPages> getPages(String userId, String itemsPerPage, String querySearch) {

        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        if (querySearch != null) {
            shoppingListMetadataList = shoppingListMetadataList.stream().filter(
                    metadata -> metadata.getShoppingListName().contains(querySearch)
            ).toList();
        }

        int numberOfPages =  Math.ceilDiv(shoppingListMetadataList.size(), Integer.parseInt(itemsPerPage));
        NumberOfPages numberOfPagesObject = new NumberOfPages(numberOfPages);

        return ResponseEntity.ok(numberOfPagesObject);
    }

    @Override
    public ResponseEntity<List<ShoppingListInfo>> getMetadata(String userId) {
        List<ShoppingListInfo> shoppingListMetadataList = metadataRepository.getShoppingListMetadata(userId);

        return ResponseEntity.ok(shoppingListMetadataList);
    }
}
