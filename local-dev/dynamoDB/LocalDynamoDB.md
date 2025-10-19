# Local DynamoDB

Follow below steps to run local DynamoDB:

### 1. Generate docker img

```bash
  docker build -t sl-dynamodb:1.0.0 local-dev/dynamoDB
```

### 2 Run docker img

```bash
  docker run --name sl-dynamodb -d -p 8000:8000 sl-dynamodb:1.0.0
```

### 3 Wait for "Setup complete!" to be displayed

```bash
  docker logs sl-dynamodb -f
```