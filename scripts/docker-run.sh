#!/usr/bin/env bash
set -euo pipefail
IMAGE="${IMAGE:-country-info:local}"
CONTAINER="${CONTAINER:-country-info}"
PORT="${PORT:-8080}"
# Set DB_* environment variables before running if your application requires MySQL.
docker rm -f "$CONTAINER" >/dev/null 2>&1 || true
docker run -d --name "$CONTAINER" --restart unless-stopped \
  -p "${PORT}:8080" \
  -e SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://host.docker.internal:3306/country_info}" \
  -e SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-root}" \
  -e SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-change-me}" \
  "$IMAGE"
echo "Container started: $CONTAINER"
echo "Logs: docker logs -f $CONTAINER"
echo "URL: http://localhost:${PORT}"
