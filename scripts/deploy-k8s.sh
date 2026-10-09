#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
NAMESPACE="${NAMESPACE:-country-info}"
IMAGE="${IMAGE:-country-info:local}"
CONTEXT="${KUBE_CONTEXT:-}"
KUBECTL=(kubectl)
if [[ -n "$CONTEXT" ]]; then KUBECTL+=(--context "$CONTEXT"); fi
"${KUBECTL[@]}" apply -f k8s/namespace.yaml
"${KUBECTL[@]}" apply -f k8s/configmap.yaml
if ! "${KUBECTL[@]}" -n "$NAMESPACE" get secret country-info-secret >/dev/null 2>&1; then
  cat >&2 <<MSG
Missing secret country-info-secret in namespace $NAMESPACE.
Create it without committing credentials:
kubectl -n $NAMESPACE create secret generic country-info-secret \\
  --from-literal=SPRING_DATASOURCE_USERNAME='YOUR_DB_USER' \\
  --from-literal=SPRING_DATASOURCE_PASSWORD='YOUR_DB_PASSWORD'
MSG
  exit 1
fi
"${KUBECTL[@]}" apply -f k8s/deployment.yaml
"${KUBECTL[@]}" -n "$NAMESPACE" set image deployment/country-info country-info="$IMAGE"
"${KUBECTL[@]}" apply -f k8s/service.yaml
"${KUBECTL[@]}" -n "$NAMESPACE" rollout status deployment/country-info --timeout=180s
"${KUBECTL[@]}" -n "$NAMESPACE" get pods,svc
