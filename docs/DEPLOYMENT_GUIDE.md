# Country Info Microservice — Deployment Guide

This guide covers local Docker, local Kubernetes on macOS, and a Jenkins pipeline. It assumes Java 17, Maven, a Spring Boot executable JAR listening on port 8080, and MySQL if your application uses the datasource configuration shown here. Adjust the database URL and environment variables to match `application.yml`.

## 1. Files to copy into your application repository

Copy `Dockerfile`, `Jenkinsfile`, and the `scripts/`, `k8s/`, and `docs/` directories into the root of your `country-info-microservice` repository. Keep the application's existing `pom.xml` and source code.

## 2. Build and test

```bash
chmod +x scripts/*.sh
./scripts/build.sh
```

This runs `mvn -B clean verify`. Resolve any failed tests before proceeding.

## 3. Run with Docker

Ensure Docker Desktop is running. Build an image:

```bash
./scripts/docker-build.sh
```

Run it:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://host.docker.internal:3306/country_info'
export SPRING_DATASOURCE_USERNAME='YOUR_DB_USER'
export SPRING_DATASOURCE_PASSWORD='YOUR_DB_PASSWORD'
./scripts/docker-run.sh
```

Check logs and stop/remove the container:

```bash
docker logs -f country-info
docker stop country-info
docker rm country-info
```

`host.docker.internal` allows a container on Docker Desktop for Mac to reach a service running on the Mac host. If MySQL runs in another container, use a shared Docker network and the database container/service name instead.

## 4. Start local Kubernetes on macOS

Install Docker Desktop and enable Kubernetes in Docker Desktop settings, or use another local cluster such as minikube. Confirm the selected context:

```bash
kubectl config get-contexts
kubectl config current-context
kubectl cluster-info
kubectl get nodes
```

If using Docker Desktop Kubernetes, build the image with Docker Desktop's daemon so the local cluster can see it:

```bash
./scripts/docker-build.sh
```

The included Deployment uses `image: country-info:local` and `imagePullPolicy: IfNotPresent`, suitable for this local workflow. If you use minikube, load the image into its image store (`minikube image load country-info:local`) or push it to a registry reachable by the cluster.

## 5. Configure database credentials

Update `k8s/configmap.yaml` with the correct JDBC URL, database name, and non-secret configuration. For a MySQL server running directly on the Mac, `host.docker.internal` often works with Docker Desktop Kubernetes, but verify connectivity for your cluster type. If the database is inside Kubernetes, use its Service DNS name, for example `mysql.<namespace>.svc.cluster.local`.

Create the namespace and secret (use your actual credentials):

```bash
kubectl apply -f k8s/namespace.yaml
kubectl -n country-info create secret generic country-info-secret \
  --from-literal=SPRING_DATASOURCE_USERNAME='YOUR_DB_USER' \
  --from-literal=SPRING_DATASOURCE_PASSWORD='YOUR_DB_PASSWORD'
```

If the secret already exists, update it with `kubectl apply` using a generated manifest, or delete and recreate it during a controlled local setup. Avoid storing plaintext credentials in source control. Production environments should use an external secrets manager or platform-managed secret integration.

## 6. Deploy to Kubernetes

```bash
./scripts/deploy-k8s.sh
kubectl -n country-info get pods,deploy,svc
kubectl -n country-info rollout status deployment/country-info
```

Forward local port 8080 to the Service's port 80:

```bash
kubectl -n country-info port-forward service/country-info 8080:80
```

In another terminal, call your application's actual endpoint, for example:

```bash
curl -i http://localhost:8080/
```

Replace `/` with a known endpoint from your API. `ClusterIP` is internal to the cluster; port-forward is the simplest local access path.

## 7. Configure Jenkins

The Jenkins agent must have Java 17, Maven (or use Maven Wrapper), Docker CLI plus access to a Docker daemon, `kubectl`, and network access to the Kubernetes API and registry. The Jenkinsfile assumes a Multibranch Pipeline and deploys only from `main`.

Create these Jenkins credentials:

1. `country-info-image-repo` — **Secret text**, value like `docker.io/YOUR_DOCKERHUB_USER/country-info`.
2. `dockerhub-credentials` — **Username with password**, for your container registry.
3. `country-info-kubeconfig` — **Secret file**, containing a kubeconfig for the target cluster. Grant this identity only the permissions it needs in the target namespace.

Create a Pipeline or Multibranch Pipeline job pointing at the repository, commit the `Jenkinsfile`, and run it. The pipeline tests, builds and pushes a uniquely tagged image, then deploys the `main` branch image. Confirm the Kubernetes context and cluster access before enabling the deployment stage. For Docker-in-Docker or remote Docker, configure the Jenkins agent according to your environment; do not assume a containerized Jenkins agent automatically has host Docker access.

## 8. Rollback

```bash
kubectl -n country-info rollout history deployment/country-info
kubectl -n country-info rollout undo deployment/country-info
kubectl -n country-info rollout status deployment/country-info
```

A rollback restores the previous Deployment revision, not database schema changes. Use Flyway or Liquibase for controlled schema migrations and make migrations backward-compatible with rolling deployments.

## Notes before production

- Verify the correct JDBC driver and DB configuration for your application.
- `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` expects the schema to exist already; use a migration tool to manage it.
- The generic TCP probes only verify that port 8080 accepts connections. If Spring Boot Actuator is present, prefer HTTP probes against `/actuator/health/liveness` and `/actuator/health/readiness` and configure Actuator exposure intentionally.
- Add Ingress/TLS or a LoadBalancer for external access; the included Service is intentionally `ClusterIP`.
- Pin images to immutable tags or digests, scan images, set resource requests/limits based on measurements, and avoid using `latest` for controlled releases.
