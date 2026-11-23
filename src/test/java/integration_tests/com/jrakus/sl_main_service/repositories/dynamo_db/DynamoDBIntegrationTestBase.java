package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.properties.DynamoDBProperties;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import com.jrakus.sl_main_service.support.DynamoDBTestContainer;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for repository integration tests.
 * <p>
 * The DynamoDB Local container is shared by all integration tests (see {@link DynamoDBTestContainer}).
 * The table is recreated before every test, so tests do not influence each other.
 */
public abstract class DynamoDBIntegrationTestBase {

    protected final DynamoDBQueryHelper dynamoDBQueryHelper = createQueryHelper();

    @BeforeEach
    void recreateTable() {
        DynamoDBTestContainer.recreateTable();
    }

    private static DynamoDBQueryHelper createQueryHelper() {
        DynamoDBProperties properties = new DynamoDBProperties();
        properties.setTableName(DynamoDBTestContainer.TABLE_NAME);

        return new DynamoDBQueryHelper(DynamoDBTestContainer.client(), properties);
    }
}
