#!/bin/bash
set -e

# Start DynamoDB Local in the background
java -jar /home/dynamodblocal/DynamoDBLocal.jar -inMemory -port 8000 -sharedDb &
DYNAMO_PID=$!

# Run setup script to create tables + insert data
/dynamodb/setup.sh

# Bring DynamoDB Local process to the foreground
wait $DYNAMO_PID