#!/usr/bin/env bash
set -u
NAMESPACE="${NAMESPACE:-country-info}"
DEPLOYMENT="${DEPLOYMENT:-country-info}"
echo '=== Cluster ==='
kubectl cluster-info || true
echo '=== Nodes ==='
kubectl get nodes -o wide || true
echo "=== Workloads in namespace: $NAMESPACE ==="
kubectl -n "$NAMESPACE" get deploy,rs,pods,svc,endpoints -o wide || true
echo '=== Recent events ==='
kubectl -n "$NAMESPACE" get events --sort-by=.lastTimestamp | tail -40 || true
POD="$(kubectl -n "$NAMESPACE" get pods -l app=country-info -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)"
if [[ -n "$POD" ]]; then
  echo "=== Describe pod: $POD ==="
  kubectl -n "$NAMESPACE" describe pod "$POD" || true
  echo '=== Current logs ==='
  kubectl -n "$NAMESPACE" logs "$POD" --all-containers=true --tail=200 || true
  echo '=== Previous container logs (if restarted) ==='
  kubectl -n "$NAMESPACE" logs "$POD" --all-containers=true --previous --tail=200 || true
else
  echo 'No pod found with label app=country-info.'
fi
