package com.jrakus.sl_main_service.api;

import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipeMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.mapper.RecipesMetadataMapper;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import com.jrakus.sl_main_service.support.DynamoDBTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.openapitools.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base class for API integration tests.
 * <p>
 * The whole application is started on a random port and talks to the shared DynamoDB Local container
 * (see {@link DynamoDBTestContainer}). The table is recreated before every test, so tests do not influence each other.
 * Requests are sent over real HTTP, so routing, validation, serialization and error handling are all exercised.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiIntegrationTestBase {

    protected static final String USER_ID = "user-1";
    protected static final String OTHER_USER_ID = "user-2";

    private static final String PK_GLOBAL = "GLOBAL#RECIPES";

    protected static final ParameterizedTypeReference<List<Recipe>> RECIPE_LIST = new ParameterizedTypeReference<>() {};
    protected static final ParameterizedTypeReference<List<RecipeInfo>> RECIPE_INFO_LIST = new ParameterizedTypeReference<>() {};
    protected static final ParameterizedTypeReference<List<ShoppingList>> SHOPPING_LIST_LIST = new ParameterizedTypeReference<>() {};
    protected static final ParameterizedTypeReference<List<ShoppingListInfo>> SHOPPING_LIST_INFO_LIST = new ParameterizedTypeReference<>() {};
    protected static final ParameterizedTypeReference<List<String>> STRING_LIST = new ParameterizedTypeReference<>() {};

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    private DynamoDBQueryHelper dynamoDBQueryHelper;

    private final RecipeMapper recipeMapper = new RecipeMapper();
    private final RecipesMetadataMapper recipesMetadataMapper = new RecipesMetadataMapper();

    @DynamicPropertySource
    static void dynamoDBProperties(DynamicPropertyRegistry registry) {
        registry.add("dynamodb.endpoint", DynamoDBTestContainer::endpoint);
        registry.add("dynamodb.tableName", () -> DynamoDBTestContainer.TABLE_NAME);
    }

    @BeforeEach
    void recreateTable() {
        DynamoDBTestContainer.recreateTable();
    }

    // ===================
    // ===== HTTP ========
    // ===================

    protected <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        return rest.getForEntity("/api/v1" + path, responseType);
    }

    protected <T> ResponseEntity<T> get(String path, ParameterizedTypeReference<T> responseType) {
        return rest.exchange("/api/v1" + path, HttpMethod.GET, null, responseType);
    }

    protected <T> ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
        return rest.postForEntity("/api/v1" + path, body, responseType);
    }

    protected <T> ResponseEntity<T> put(String path, Object body, Class<T> responseType) {
        return rest.exchange("/api/v1" + path, HttpMethod.PUT, new HttpEntity<>(body), responseType);
    }

    protected <T> ResponseEntity<T> delete(String path, Class<T> responseType) {
        return rest.exchange("/api/v1" + path, HttpMethod.DELETE, null, responseType);
    }

    // =====================
    // ===== BUILDERS ======
    // =====================

    protected static RecipeItem item(String name, float quantity, String unit, String category) {
        return new RecipeItem(name, quantity, unit, category);
    }

    protected static RecipeCreate recipeCreate(String name, RecipeItem... items) {
        RecipeCreate recipeCreate = new RecipeCreate().name(name).isGlobal(false);
        for (RecipeItem item : items) {
            recipeCreate.addItemsItem(item);
        }
        return recipeCreate;
    }

    protected static ShoppingListCreate shoppingListCreate(String name, CategorizedItems... categories) {
        ShoppingListCreate shoppingListCreate = new ShoppingListCreate().name(name);
        for (CategorizedItems category : categories) {
            shoppingListCreate.addItemsPerCategoryItem(category);
        }
        return shoppingListCreate;
    }

    protected static CategorizedItems category(String name, ShoppingListItem... items) {
        CategorizedItems category = new CategorizedItems().category(name);
        for (ShoppingListItem item : items) {
            category.addItemsItem(item);
        }
        return category;
    }

    protected static ShoppingListItem listItem(String name, float quantity, String unit, boolean purchased) {
        return new ShoppingListItem(name, quantity, unit, purchased);
    }

    // =========================
    // ===== API SHORTCUTS =====
    // =========================

    protected Recipe createRecipe(String userId, RecipeCreate recipeCreate) {
        ResponseEntity<Recipe> response = post("/recipes/" + userId, recipeCreate, Recipe.class);
        assertEquals(201, response.getStatusCode().value());
        return response.getBody();
    }

    protected ShoppingList createShoppingList(String userId, ShoppingListCreate shoppingListCreate) {
        ResponseEntity<ShoppingList> response = post("/shoppingLists/" + userId, shoppingListCreate, ShoppingList.class);
        assertEquals(201, response.getStatusCode().value());
        return response.getBody();
    }

    // ============================
    // ===== GLOBAL RECIPES =======
    // ============================

    // the API has no way to create public recipes, so they (and their metadata) are inserted directly

    /**
     * Saves public recipes together with the global metadata that lists them (in the given order).
     */
    protected void publishPublicRecipes(Recipe... recipes) {
        List<RecipeInfo> metadata = new ArrayList<>();

        for (Recipe recipe : recipes) {
            dynamoDBQueryHelper.saveSingleItem(
                    recipeMapper.toDynamoDBItem(PK_GLOBAL, "RECIPE#" + recipe.getRecipeId(), recipe)
            );
            metadata.add(new RecipeInfo().id(recipe.getRecipeId()).recipeName(recipe.getName()).isGlobal(true));
        }

        dynamoDBQueryHelper.saveSingleItem(recipesMetadataMapper.toDynamoDBItem(PK_GLOBAL, "METADATA", metadata));
    }

    protected Recipe publicRecipe(String recipeId, String name, RecipeItem... items) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Recipe()
                .recipeId(recipeId)
                .name(name)
                .createdAt(now)
                .updatedAt(now)
                .items(List.of(items));
    }
}
