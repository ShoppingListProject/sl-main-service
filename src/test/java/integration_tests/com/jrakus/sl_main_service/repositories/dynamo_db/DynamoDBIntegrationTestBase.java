package com.jrakus.sl_main_service.repositories.dynamo_db;

import com.jrakus.sl_main_service.properties.DynamoDBProperties;
import com.jrakus.sl_main_service.repositories.dynamo_db.utils.DynamoDBQueryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;

/**
 * Base class for repository integration tests.
 * <p>
 * A single DynamoDB Local container is shared by all test classes (it is started once, on first use,
 * and removed by Testcontainers when the JVM exits). The table is recreated before every test,
 * so tests do not influence each other.
 */
public abstract class DynamoDBIntegrationTestBase {

    private static final String TABLE_NAME = "SLTable";
    private static final int DYNAMODB_PORT = 8000;

    private static final GenericContainer<?> DYNAMODB =
            new GenericContainer<>("amazon/dynamodb-local:3.1.0").withExposedPorts(DYNAMODB_PORT);

    private static final DynamoDbClient DYNAMODB_CLIENT;

    static {
        DYNAMODB.start();

        DYNAMODB_CLIENT = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://" + DYNAMODB.getHost() + ":" + DYNAMODB.getMappedPort(DYNAMODB_PORT)))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("fakeKeyId", "fakeKey")))
                .build();
    }

    protected final DynamoDBQueryHelper dynamoDBQueryHelper = createQueryHelper();

    @BeforeEach
    void recreateTable() {
        if (DYNAMODB_CLIENT.listTables().tableNames().contains(TABLE_NAME)) {
            DYNAMODB_CLIENT.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
        }

        // same schema as local-dev/dynamoDB/table.json
        DYNAMODB_CLIENT.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName("PK").attributeType(ScalarAttributeType.S).build(),
                        AttributeDefinition.builder().attributeName("SK").attributeType(ScalarAttributeType.S).build())
                .keySchema(
                        KeySchemaElement.builder().attributeName("PK").keyType(KeyType.HASH).build(),
                        KeySchemaElement.builder().attributeName("SK").keyType(KeyType.RANGE).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build());
    }

    private static DynamoDBQueryHelper createQueryHelper() {
        DynamoDBProperties properties = new DynamoDBProperties();
        properties.setTableName(TABLE_NAME);

        return new DynamoDBQueryHelper(DYNAMODB_CLIENT, properties);
    }
}
