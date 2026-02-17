## Publishing new version of TS API Models

To publish new version of API Models follow below steps:

#### 1. Install dependencies
```bash
  npm install
```

#### 2. Remove folder 'generated'

#### 3. Generate models from OpenAPI spec
```bash
  npm run generate
```

#### 3. Update index.ts if:
1. you added new models that require importing.
1. you removed models that were imported previously.

#### 4. Compile file from ts to js
```bash
  npx tsc
```

#### 5. Update package version
```bash
  npm version patch
```

#### 6. Publish new version
```bash
  npm publish --access public
```

#### 7. Commit newest changes