#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v mvn >/dev/null || { echo "Maven (mvn) is required." >&2; exit 1; }
mvn -B clean verify
