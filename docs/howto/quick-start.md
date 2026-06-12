# How to run Alf.io locally

## Prerequisites

- Docker installed and running

```docker
docker run -d --name alfio-db -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_DB=alfio --restart unless-stopped postgres
```