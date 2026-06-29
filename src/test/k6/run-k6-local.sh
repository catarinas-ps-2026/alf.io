#!/usr/bin/env bash

set -euo pipefail

# Ensure we are at the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
cd "${PROJECT_ROOT}"

echo "Starting PostgreSQL database container..."
docker compose up -d db

echo "Building Spring Boot application jar..."
./gradlew bootJar

API_KEY="my-performance-test-api-key"
PORT=8080

echo "Starting alf.io application in background..."
ALFIO_OVERRIDE_SYSTEM_SETTINGS_SYSTEM_API_KEY="${API_KEY}" \
java -Ddatasource.url="jdbc:postgresql://localhost:5432/alfio" \
     -Ddatasource.username="postgres" \
     -Ddatasource.password="password" \
     -Dspring.profiles.active=dev \
     -jar build/libs/alfio-*-boot.jar > app.log 2>&1 &

APP_PID=$!

# Ensure cleanup on exit
cleanup() {
    echo "Stopping alf.io application (PID: ${APP_PID})..."
    kill "${APP_PID}" || true
    wait "${APP_PID}" 2>/dev/null || true
    echo "Cleanup complete."
}
trap cleanup EXIT

echo "Waiting for alf.io to start up on port ${PORT}..."
MAX_ATTEMPTS=60
ATTEMPT=0
while true; do
    if curl --output /dev/null --silent --fail "http://localhost:${PORT}/healthz"; then
        echo "alf.io is healthy!"
        break
    fi
    ATTEMPT=$((ATTEMPT + 1))
    if [ ${ATTEMPT} -eq ${MAX_ATTEMPTS} ]; then
        echo "Error: Timeout waiting for alf.io to start."
        cat app.log
        exit 1
    fi
    sleep 1
done

echo "Running k6 performance test..."
k6 run src/test/k6/performance-test.js --env BASE_URL="http://localhost:${PORT}" --env API_KEY="${API_KEY}"
