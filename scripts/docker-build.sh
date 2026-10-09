#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
IMAGE="${IMAGE:-country-info:local}"
mvn -B clean package -DskipTests=false
docker build --pull -t "$IMAGE" .
echo "Built image: $IMAGE"
