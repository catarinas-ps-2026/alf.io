# How to run Alf.io locally

## Prerequisites

- Docker installed and running

```docker
docker run \
  --name alfio-db \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 \
  -d postgres
```
- then verify the conection with
```docker
docker logs alfio-db
```
