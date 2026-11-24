package com.jrakus.sl_main_service.api;

import org.junit.jupiter.api.Test;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListsApiIntegrationTests extends ApiIntegrationTestBase {

    // ======================
    // ===== CREATE =========
    // ======================

    @Test
    void shouldCreateShoppingListAndReturnIt() {

        // given
        ShoppingListCreate request = shoppingListCreate(
                "weekend",
                category("Dairy", listItem("milk", 2f, "l", false))
        );

        // when
        ResponseEntity<ShoppingList> response = post("/shoppingLists/" + USER_ID, request, ShoppingList.class);

        // then
        assertEquals(201, response.getStatusCode().value());
        ShoppingList shoppingList = response.getBody();
        assertNotNull(shoppingList.getShoppingListId());
        assertEquals("weekend", shoppingList.getName());
        assertEquals(1, shoppingList.getItemsPerCategory().size());
        assertEquals("Dairy", shoppingList.getItemsPerCategory().get(0).getCategory());
        assertEquals("milk", shoppingList.getItemsPerCategory().get(0).getItems().get(0).getName());
        assertEquals(shoppingList.getCreatedAt(), shoppingList.getUpdatedAt());
    }

    @Test
    void shouldAddCreatedShoppingListToMetadataAndList() {

        // given
        ShoppingList created = createShoppingList(USER_ID, shoppingListCreate("weekend"));

        // when
        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody();
        List<ShoppingList> lists = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody();

        // then
        assertEquals(1, metadata.size());
        assertEquals(created.getShoppingListId(), metadata.get(0).getId());
        assertEquals("weekend", metadata.get(0).getShoppingListName());
        assertEquals(List.of(created.getShoppingListId()), lists.stream().map(ShoppingList::getShoppingListId).toList());
    }

    // ======================
    // ===== UPDATE =========
    // ======================

    @Test
    void shouldUpdateShoppingListAndItsMetadata() {

        // given
        ShoppingList created = createShoppingList(USER_ID, shoppingListCreate("weekend"));
        createShoppingList(USER_ID, shoppingListCreate("party"));

        // when
        ResponseEntity<ShoppingList> response = put(
                "/shoppingLists/" + USER_ID + "/" + created.getShoppingListId(),
                shoppingListCreate("holiday", category("Meat", listItem("pork", 1f, "kg", true))),
                ShoppingList.class
        );

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("holiday", response.getBody().getName());
        assertEquals(created.getCreatedAt(), response.getBody().getCreatedAt());
        assertTrue(response.getBody().getItemsPerCategory().get(0).getItems().get(0).getPurchased());

        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody();
        assertEquals(List.of("holiday", "party"), metadata.stream().map(ShoppingListInfo::getShoppingListName).toList());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownShoppingList() {

        // when
        ResponseEntity<ErrorResponse> response = put(
                "/shoppingLists/" + USER_ID + "/unknown", shoppingListCreate("holiday"), ErrorResponse.class
        );

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(Code.SHOPPING_LIST_NOT_FOUND, response.getBody().getCode());
    }

    // ======================
    // ===== DELETE =========
    // ======================

    @Test
    void shouldDeleteShoppingListAndRemoveItFromMetadata() {

        // given
        ShoppingList weekend = createShoppingList(USER_ID, shoppingListCreate("weekend"));
        ShoppingList party = createShoppingList(USER_ID, shoppingListCreate("party"));

        // when
        ResponseEntity<ShoppingList> response = delete(
                "/shoppingLists/" + USER_ID + "/" + weekend.getShoppingListId(), ShoppingList.class
        );

        // then
        assertEquals(200, response.getStatusCode().value());
        assertEquals(weekend.getShoppingListId(), response.getBody().getShoppingListId());

        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody();
        assertEquals(List.of(party.getShoppingListId()), metadata.stream().map(ShoppingListInfo::getId).toList());

        List<ShoppingList> lists = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody();
        assertEquals(List.of(party.getShoppingListId()), lists.stream().map(ShoppingList::getShoppingListId).toList());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownShoppingList() {

        // when
        ResponseEntity<ErrorResponse> response = delete("/shoppingLists/" + USER_ID + "/unknown", ErrorResponse.class);

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(Code.SHOPPING_LIST_NOT_FOUND, response.getBody().getCode());
    }

    @Test
    void shouldNotDeleteShoppingListOfAnotherUser() {

        // given
        ShoppingList created = createShoppingList(USER_ID, shoppingListCreate("weekend"));

        // when
        ResponseEntity<ErrorResponse> response = delete(
                "/shoppingLists/" + OTHER_USER_ID + "/" + created.getShoppingListId(), ErrorResponse.class
        );

        // then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(1, get("/shoppingLists/metadata/" + USER_ID, SHOPPING_LIST_INFO_LIST).getBody().size());
    }

    // ======================
    // ===== LIST ===========
    // ======================

    @Test
    void shouldReturnEmptyListForUserWithoutShoppingLists() {

        // when
        ResponseEntity<List<ShoppingList>> response = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST);

        // then
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldReturnShoppingListsInMetadataOrderWithPagination() {

        // given
        List<String> ids = new java.util.ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ids.add(createShoppingList(USER_ID, shoppingListCreate("list-" + i)).getShoppingListId());
        }

        // when
        List<ShoppingList> all = get("/shoppingLists/" + USER_ID, SHOPPING_LIST_LIST).getBody();
        List<ShoppingList> secondPage = get("/shoppingLists/" + USER_ID + "?offset=2&limit=2", SHOPPING_LIST_LIST).getBody();
        List<ShoppingList> beyondEnd = get("/shoppingLists/" + USER_ID + "?offset=10", SHOPPING_LIST_LIST).getBody();

        // then
        assertEquals(ids, all.stream().map(ShoppingList::getShoppingListId).toList());
        assertEquals(ids.subList(2, 4), secondPage.stream().map(ShoppingList::getShoppingListId).toList());
        assertTrue(beyondEnd.isEmpty());
    }

    @Test
    void shouldRejectInvalidPaginationParameters() {

        // when / then
        assertEquals(400, get("/shoppingLists/" + USER_ID + "?offset=-1", String.class).getStatusCode().value());
        assertEquals(400, get("/shoppingLists/" + USER_ID + "?limit=0", String.class).getStatusCode().value());
        assertEquals(400, get("/shoppingLists/" + USER_ID + "?limit=101", String.class).getStatusCode().value());
    }

    @Test
    void shouldSearchShoppingListsIgnoringCase() {

        // given
        createShoppingList(USER_ID, shoppingListCreate("Weekend"));
        createShoppingList(USER_ID, shoppingListCreate("Party"));

        // when
        List<ShoppingList> lists = get("/shoppingLists/" + USER_ID + "?querySearch=WEEK", SHOPPING_LIST_LIST).getBody();

        // then
        assertEquals(1, lists.size());
        assertEquals("Weekend", lists.get(0).getName());
    }

    @Test
    void shouldNotShowShoppingListsOfAnotherUser() {

        // given
        createShoppingList(USER_ID, shoppingListCreate("weekend"));

        // when
        List<ShoppingList> lists = get("/shoppingLists/" + OTHER_USER_ID, SHOPPING_LIST_LIST).getBody();
        List<ShoppingListInfo> metadata = get("/shoppingLists/metadata/" + OTHER_USER_ID, SHOPPING_LIST_INFO_LIST).getBody();

        // then
        assertTrue(lists.isEmpty());
        assertTrue(metadata.isEmpty());
    }

    // ======================
    // ===== PAGES ==========
    // ======================

    @Test
    void shouldCountPages() {

        // given
        for (int i = 1; i <= 5; i++) {
            createShoppingList(USER_ID, shoppingListCreate("list-" + i));
        }

        // when
        NumberOfPages pages = get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=2", NumberOfPages.class).getBody();
        NumberOfPages filtered = get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=2&querySearch=list-3", NumberOfPages.class).getBody();

        // then
        assertEquals(3, pages.getNumberOfPages());
        assertEquals(1, filtered.getNumberOfPages());
    }

    @Test
    void shouldReturnZeroPagesForUserWithoutShoppingLists() {

        // when
        NumberOfPages pages = get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=10", NumberOfPages.class).getBody();

        // then
        assertEquals(0, pages.getNumberOfPages());
    }

    @Test
    void shouldRejectInvalidItemsPerPage() {

        // when / then
        assertEquals(400, get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=abc", String.class).getStatusCode().value());
        assertEquals(400, get("/shoppingLists/pages/" + USER_ID + "?itemsPerPage=0", String.class).getStatusCode().value());
        assertEquals(400, get("/shoppingLists/pages/" + USER_ID, String.class).getStatusCode().value());
    }
}
