package com.jrakus.sl_main_service.repositories.dynamo_db.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.model.ShoppingListInfo;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShoppingListsMetadataMapperUnitTests {

    private static final String PK = "USER#1234";
    private static final String SK = "METADATA#SHOPPING_LISTS";

    private static final ShoppingListInfo WEEKEND =
            new ShoppingListInfo("weekend shopping", "1234", OffsetDateTime.parse("2025-09-27T15:16:05.951250500Z"));
    private static final ShoppingListInfo PARTY =
            new ShoppingListInfo("party", "1235", OffsetDateTime.parse("2025-10-28T15:16:05.951250500Z"));
    private static final ShoppingListInfo DINNER =
            new ShoppingListInfo("dinner with friends", "1236", OffsetDateTime.parse("2025-10-28T12:16:05.951450500Z"));

    private final ShoppingListsMetadataMapper shoppingListsMetadataMapper = new ShoppingListsMetadataMapper();

    static Stream<Arguments> shoppingListInfoLists() {
        return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(WEEKEND)),
                Arguments.of(List.of(WEEKEND, PARTY, DINNER))
        );
    }

    @ParameterizedTest
    @MethodSource("shoppingListInfoLists")
    void shouldMapMetadataFromDynamoDB(List<ShoppingListInfo> shoppingListInfos) {

        // given
        Map<String, AttributeValue> metadata = createMetadataMap(shoppingListInfos);

        // when
        List<ShoppingListInfo> result = shoppingListsMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(shoppingListInfos, result);
    }

    @ParameterizedTest
    @MethodSource("shoppingListInfoLists")
    void shouldMapMetadataToDynamoDB(List<ShoppingListInfo> shoppingListInfos) {

        // when
        Map<String, AttributeValue> result = shoppingListsMetadataMapper.toDynamoDBItem(PK, SK, shoppingListInfos);

        // then
        assertEquals(createMetadataMap(shoppingListInfos), result);
    }

    @ParameterizedTest
    @MethodSource("shoppingListInfoLists")
    void shouldKeepMetadataUnchangedAfterRoundTrip(List<ShoppingListInfo> shoppingListInfos) {

        // when
        Map<String, AttributeValue> metadata = shoppingListsMetadataMapper.toDynamoDBItem(PK, SK, shoppingListInfos);
        List<ShoppingListInfo> result = shoppingListsMetadataMapper.fromDynamoDBItem(metadata);

        // then
        assertEquals(shoppingListInfos, result);
    }

    private Map<String, AttributeValue> createMetadataMap(List<ShoppingListInfo> shoppingListInfos) {
        return Map.of(
                "PK", AttributeValue.fromS(PK),
                "SK", AttributeValue.fromS(SK),
                "metadata", AttributeValue.fromL(
                        shoppingListInfos.stream().map(this::createShoppingListInfoAsMap).toList()
                )
        );
    }

    private AttributeValue createShoppingListInfoAsMap(ShoppingListInfo info) {
        return AttributeValue.fromM(Map.of(
                "shoppingListName", AttributeValue.fromS(info.getShoppingListName()),
                "id", AttributeValue.fromS(info.getId()),
                "updatedAt", AttributeValue.fromS(info.getUpdatedAt().toString())
        ));
    }
}
