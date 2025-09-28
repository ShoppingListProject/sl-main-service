#!/bin/bash

# Required to use aws-cli
export AWS_ACCESS_KEY_ID=dummy
export AWS_SECRET_ACCESS_KEY=dummy
export AWS_DEFAULT_REGION=us-west-2

DYNAMO_PORT=8000
TABLES_JSON="/dynamodb/table.json"
ITEMS_JSON="/dynamodb/items.json"

TIMEOUT=30  # seconds
SECONDS_PASSED=0


# Wait until DynamoDB Local is ready
echo "Waiting for DynamoDB to be ready..."
until aws dynamodb list-tables --endpoint-url http://localhost:$DYNAMO_PORT >/dev/null 2>&1; do

    sleep 2

    SECONDS_PASSED=$((SECONDS_PASSED + 2))

    if [ $SECONDS_PASSED -ge $TIMEOUT ]; then
        echo "DynamoDB did not start in time!"
        exit 1
    fi

done
echo "DynamoDB is ready!"


# Create table
echo "Creating table..."
aws dynamodb create-table \
    --cli-input-json file://$TABLES_JSON \
    --endpoint-url http://localhost:$DYNAMO_PORT


# Insert items
echo "Inserting items using batch-write-item..."
aws dynamodb batch-write-item \
    --request-items file://$ITEMS_JSON \
    --endpoint-url http://localhost:$DYNAMO_PORT


echo "Setup complete!"
