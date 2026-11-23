package com.jrakus.sl_main_service.support;

import org.testcontainers.containers.GenericContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;

/**
 * A single DynamoDB Local container shared by all integration tests (it is started once, on first use,
 * and removed by Testcontainers when the JVM exits).
 */
public final class DynamoDBTestContainer {

    public static final String TABLE_NAME = "SLTable";

    private static final int DYNAMODB_PORT = 8000;

    private static final GenericContainer<?> DYNAMODB =
            new GenericContainer<>("amazon/dynamodb-local:3.1.0").withExposedPorts(DYNAMODB_PORT);

    private static final DynamoDbClient CLIENT;

    static {
        DYNAMODB.start();

        CLIENT = DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("fakeKeyId", "fakeKey")))
                .build();
    }

    private DynamoDBTestContainer() {
    }

    public static String endpoint() {
        return "http://" + DYNAMODB.getHost() + ":" + DYNAMODB.getMappedPort(DYNAMODB_PORT);
    }

    public static DynamoDbClient client() {
        return CLIENT;
    }

    /**
     * Drops the table (if it exists) and creates it again, empty.
     */
    public static void recreateTable() {
        if (CLIENT.listTables().tableNames().contains(TABLE_NAME)) {
            CLIENT.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
        }

        // same schema as local-dev/dynamoDB/table.json
        CLIENT.createTable(CreateTableRequest.builder()
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
}
