package com.jrakus.sl_main_service.configuration;
import org.springframework.stereotype.Component;

@Component
public class DynamoDBTableConfig {
    private final String tableName;

    public DynamoDBTableConfig(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
