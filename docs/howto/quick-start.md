# How to run Alf.io locally

## Prerequisites

- Docker installed and running

```bash
docker run \
  --name alfio-db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  -d postgres
```
- then verify the conection with
```bash
docker logs alfio-db
```

Finally run the application on graddle (see if there's a faster way to run the application but it does not work right now)
```bash
./gradlew -Pprofile=dev :bootRun
```