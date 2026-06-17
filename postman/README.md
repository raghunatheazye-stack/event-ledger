# Postman VS Code import

Import these files with the Postman VS Code extension:

1. Open the Postman icon in the VS Code activity bar.
2. Sign in and select a workspace if prompted.
3. Click **Import**.
4. Import `Event Ledger.postman_collection.json`.
5. Import `Event Ledger.postman_environment.json`.
6. Select the **Event Ledger - Local** environment.

The collection contains two folders:

- `event-gateway` for the public API on `http://localhost:8080`
- `account-service` for the internal ledger API on `http://localhost:8081`

Start the services first with either Maven or Docker Compose:

```bash
docker compose up --build
```

