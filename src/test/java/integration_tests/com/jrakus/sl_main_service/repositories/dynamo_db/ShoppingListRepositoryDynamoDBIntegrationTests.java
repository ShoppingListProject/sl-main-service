package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.ShoppingListMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.model.CategorizedItems;
import org.openapitools.model.ShoppingList;
import org.openapitools.model.ShoppingListItem;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListRepositoryDynamoDBIntegrationTests extends DynamoDBIntegrationTestBase {

    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";

    private final ShoppingListRepositoryDynamoDB shoppingListRepository =
            new ShoppingListRepositoryDynamoDB(dynamoDBQueryHelper, new ShoppingListMapper());

    // ================
    // ===== READ =====
    // ================

    @Test
    void shouldSaveAndReadShoppingListForUser() {

        // given
        ShoppingList shoppingList = createShoppingList("1", "weekend");

        // when
        shoppingListRepository.saveShoppingListForUser(USER_ID, shoppingList);

        // then
        assertEquals(Optional.of(shoppingList), shoppingListRepository.getUserShoppingListById(USER_ID, "1"));
    }

    @Test
    void shouldSaveAndReadShoppingListWithoutItems() {

        // given
        ShoppingList shoppingList = createShoppingList("1", "weekend").itemsPerCategory(List.of());

        // when
        shoppingListRepository.saveShoppingListForUser(USER_ID, shoppingList);

        // then
        assertEquals(Optional.of(shoppingList), shoppingListRepository.getUserShoppingListById(USER_ID, "1"));
    }

    @Test
    void shouldKeepPurchasedFlagAndQuantityOfItems() {

        // given
        ShoppingList shoppingList = createShoppingList("1", "weekend").itemsPerCategory(List.of(
                new CategorizedItems().category("Bread").items(List.of(
                        new ShoppingListItem().name("rolls").unit("pieces").quantity(6.5f).purchased(true),
                        new ShoppingListItem().name("bagel").unit("pieces").quantity(2f).purchased(false)
                ))
        ));

        // when
        shoppingListRepository.saveShoppingListForUser(USER_ID, shoppingList);

        // then
        assertEquals(Optional.of(shoppingList), shoppingListRepository.getUserShoppingListById(USER_ID, "1"));
    }

    @Test
    void shouldReturnEmptyWhenShoppingListDoesNotExist() {

        // when
        Optional<ShoppingList> result = shoppingListRepository.getUserShoppingListById(USER_ID, "missing");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnShoppingListOfOtherUserById() {

        // given
        shoppingListRepository.saveShoppingListForUser(OTHER_USER_ID, createShoppingList("1", "weekend"));

        // when
        Optional<ShoppingList> result = shoppingListRepository.getUserShoppingListById(USER_ID, "1");

        // then
        assertTrue(result.isEmpty());
    }

    // ======================
    // ===== READ MANY ======
    // ======================

    @Test
    void shouldReturnOnlyRequestedShoppingListsOfUser() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("2", "party"));
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("3", "holidays"));
        shoppingListRepository.saveShoppingListForUser(OTHER_USER_ID, createShoppingList("4", "work"));

        // when
        List<ShoppingList> result = shoppingListRepository.getShoppingListsForUser(USER_ID, List.of("1", "3", "4"));

        // then
        assertEquals(List.of("1", "3"), shoppingListIds(result));
    }

    @Test
    void shouldReturnShoppingListsInRequestedOrder() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("2", "party"));
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("3", "holidays"));

        // when
        List<ShoppingList> result = shoppingListRepository.getShoppingListsForUser(USER_ID, List.of("3", "1", "2"));

        // then
        assertEquals(List.of("3", "1", "2"), shoppingListIds(result));
    }

    @Test
    void shouldSkipMissingShoppingListsWhenReadingMany() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));

        // when
        List<ShoppingList> result = shoppingListRepository.getShoppingListsForUser(USER_ID, List.of("missing", "1"));

        // then
        assertEquals(List.of("1"), shoppingListIds(result));
    }

    @Test
    void shouldReturnEmptyListForEmptyIds() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));

        // when
        List<ShoppingList> result = shoppingListRepository.getShoppingListsForUser(USER_ID, List.of());

        // then
        assertTrue(result.isEmpty());
    }

    // =====================================
    // ===== OVERWRITE AND DELETE ==========
    // =====================================

    @Test
    void shouldOverwriteExistingShoppingList() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));
        ShoppingList updated = createShoppingList("1", "weekend v2").itemsPerCategory(List.of());

        // when
        shoppingListRepository.saveShoppingListForUser(USER_ID, updated);

        // then
        assertEquals(Optional.of(updated), shoppingListRepository.getUserShoppingListById(USER_ID, "1"));
    }

    @Test
    void shouldDeleteShoppingListForUser() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));

        // when
        shoppingListRepository.deleteShoppingListForUser(USER_ID, "1");

        // then
        assertTrue(shoppingListRepository.getUserShoppingListById(USER_ID, "1").isEmpty());
    }

    @Test
    void shouldDeleteOnlyGivenShoppingList() {

        // given
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("1", "weekend"));
        shoppingListRepository.saveShoppingListForUser(USER_ID, createShoppingList("2", "party"));
        shoppingListRepository.saveShoppingListForUser(OTHER_USER_ID, createShoppingList("1", "work"));

        // when
        shoppingListRepository.deleteShoppingListForUser(USER_ID, "1");

        // then
        assertTrue(shoppingListRepository.getUserShoppingListById(USER_ID, "1").isEmpty());
        assertTrue(shoppingListRepository.getUserShoppingListById(USER_ID, "2").isPresent());
        assertTrue(shoppingListRepository.getUserShoppingListById(OTHER_USER_ID, "1").isPresent());
    }

    @Test
    void shouldNotFailWhenDeletingMissingShoppingList() {

        // when / then
        assertDoesNotThrow(() -> shoppingListRepository.deleteShoppingListForUser(USER_ID, "missing"));
    }

    // ===================
    // ===== HELPERS =====
    // ===================

    private List<String> shoppingListIds(List<ShoppingList> shoppingLists) {
        return shoppingLists.stream().map(ShoppingList::getShoppingListId).toList();
    }

    private ShoppingList createShoppingList(String shoppingListId, String name) {
        return new ShoppingList()
                .shoppingListId(shoppingListId)
                .name(name)
                .createdAt(OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z"))
                .updatedAt(OffsetDateTime.parse("2026-11-02T12:06:15.951250500Z"))
                .itemsPerCategory(List.of(
                        new CategorizedItems().category("Vegetables & Fruits").items(List.of(
                                new ShoppingListItem().name("cucumber").unit("kg").quantity(1f).purchased(false)
                        )),
                        new CategorizedItems().category("Dairy & Eggs").items(List.of(
                                new ShoppingListItem().name("milk").unit("l").quantity(2f).purchased(true)
                        ))
                ));
    }
}
