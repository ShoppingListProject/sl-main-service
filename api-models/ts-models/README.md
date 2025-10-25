## Publishing new version of TS API Models

To publish new version of API Models follow below steps:

1) Install dependencies
```bash
  npm install
```

2) Generate models from OpenAPI spec
```bash
  npm run generate
```

3) Compile file from ts to js
```bash
  npm run compile
```

4) Publish new version
```bash
  npm run release
```