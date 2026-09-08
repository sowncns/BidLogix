# Docker Setup Using Go PostgreSQL

The Go backend at `D:/Projects/qs-crm/qs-crm-be` uses this database config:

- database: `qs_crm`
- user: `admin`
- password: `admin`
- host from Windows: `localhost`
- port: `5432`
- API prefix: `/api/v1`

The Java Docker setup now uses its own compose PostgreSQL service (`postgres`) and can be restored from that Go backup.

## Build And Run

1. From `D:/Projects/BackendQS`, run:

```powershell
docker compose --env-file .env.docker up --build backend postgres redis
```

3. Test health:

```powershell
curl http://localhost:8081/api/v1/healthz
```

4. In Postman, set collection variable `baseUrl` to:

```text
http://localhost:8081/api/v1
```

## Notes

- The `postgres` service uses `backend_db/postgres/secure_password_123` by default and publishes to host port `5434` to avoid clashing with the Go PostgreSQL on `5432`.
- If the PostgreSQL credentials differ, edit `.env.docker` values `POSTGRES_*`.
- `FLYWAY_ENABLED=true` means Java will apply its migrations to the Java PostgreSQL database. Set it to `false` if you only want to connect without changing schema.
