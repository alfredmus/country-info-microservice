# Country Info Integration Microservice

Submission-ready reference implementation for the Integration Microservices Engineer assessment.

## 1. What is implemented(`Submission checklist`)

- [x] Spring Boot REST API.
- [x] POST country import endpoint accepting `{"name":"Kenya"}`.
- [x] Sentence-case normalization before integration.
- [x] SOAP call 1: `CountryISOCode(sCountryName)` to resolve ISO code.
- [x] SOAP call 2: `FullCountryInfo(sCountryISOCode)` to retrieve full country data.
- [x] JPA/MySQL persistence for `CountryInfo` and `Language`.
- [x] CRUD REST endpoints.
- [x] Validation and consistent JSON errors.
- [x] Actuator health/metrics endpoints.
- [x] Stateless application pods and Kubernetes HPA.
- [x] Kubernetes manifests
- [x] Kubernetes readiness/liveness probes.
- [x] Docker and Docker Compose.
- [x] Unit test covering the two-step SOAP flow.
- [x] Error handling and HTTP status codes
- [x] Structured operational logging.
- [x] Deployment and troubleshooting guide

The assessment explicitly asks for the two SOAP calls, the two models, CRUD APIs, Kubernetes scripts and deployment/troubleshooting guidance. 

Run `mvn clean test` and `docker compose up --build` before submission.

## 2. Architecture

#### Client
          |
          v
#### REST Controller
          |
          v
#### CountryInfoService
            |--------------------> CountryInfoRepository ----> MySQL
            |
            +--> CountrySoapClient
                  | 1. CountryISOCode(countryName)
                  | 2. FullCountryInfo(isoCode)
                  v
               CountryInfoService SOAP

The application is stateless: country records live in MySQL, so multiple API pods can serve requests behind the Kubernetes Service.

## 3. API

### Import from SOAP

`POST /api/v1/countries/import`

```json
{"name":"kenya"}
```

The service normalizes this to `Kenya`, resolves the ISO code, retrieves full country information, persists it and returns the saved record.

### Fetch all

`GET /api/v1/countries`

### Fetch by ID

`GET /api/v1/countries/{id}`

### Update

`PUT /api/v1/countries/{id}`

Example:

```json
{
  "isoCode":"KE",
  "name":"Kenya",
  "capitalCity":"Nairobi",
  "phoneCode":"254",
  "continentCode":"AF",
  "currencyIsoCode":"KES",
  "countryFlag":"http://example/kenya.jpg",
  "languages":[
    {"isoCode":"sw","name":"Swahili"},
    {"isoCode":"en","name":"English"}
  ]
}
```

### Delete

`DELETE /api/v1/countries/{id}`

### Sample service logs
2026-10-09T10:20:42.255+03:00 | Severity=SUCCESS | MicroService=country-info-microservice | TransactionID= | Transaction=202 | Process=success deleting country record to database. | ProcessDuration=208 | Identity=1791530442254 | SourceSystem=country-info-microservice | TargetSystem=country-info-microservice | ResponseCode=202 | Response= | ErrorDescription= | RequestPayload= | ResponsePayload= | RequestHeaders=

2026-10-09T10:21:16.009+03:00 | Severity=ERROR | MicroService=country-info-microservice | TransactionID= | Transaction=500 INTERNAL_SERVER_ERROR | Process=error saving country record to database. | ProcessDuration=3986 | Identity=1791530476009 | SourceSystem=country-info-microservice | TargetSystem=country-info-microservice | ResponseCode=500 | Response= | ErrorDescription= | RequestPayload= | ResponsePayload= | RequestHeaders=


## 4. Local run

Prerequisites:

- JDK 21
- Maven 3.9+
- Docker Desktop (recommended)
- MySQL 8.x if not using Compose

Build:

```bash
mvn clean test
mvn clean package
```

Run MySQL and the application:

```bash
docker compose up --build
```

The API is then available on `http://localhost:8080`.

## 5. Test the complete integration

```bash
curl -X POST http://localhost:8080/api/v1/countries/import ^
  -H "Content-Type: application/json" ^
  -d "{"name":"kenya"}"
```

Then:

```bash
curl http://localhost:8080/api/v1/countries
curl http://localhost:8080/api/v1/countries/1
```

Update:

```bash
curl -X PUT http://localhost:8080/api/v1/countries/1 ^
  -H "Content-Type: application/json" ^
  -d "{"isoCode":"KE","name":"Kenya","capitalCity":"Nairobi","phoneCode":"254","continentCode":"AF","currencyIsoCode":"KES","countryFlag":"","languages":[{"isoCode":"en","name":"English"}]}"
```

Delete:

```bash
curl -X DELETE http://localhost:8080/api/v1/countries/1
```

Health:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## 6. SOAP verification in SoapUI

Create a SOAP project from:

`http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL`

For `CountryISOCode`, the request body should conceptually be:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <CountryISOCode xmlns="http://www.oorsprong.org/websamples.countryinfo">
      <sCountryName>Kenya</sCountryName>
    </CountryISOCode>
  </soap:Body>
</soap:Envelope>
```

For `FullCountryInfo`:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <FullCountryInfo xmlns="http://www.oorsprong.org/websamples.countryinfo">
      <sCountryISOCode>KE</sCountryISOCode>
    </FullCountryInfo>
  </soap:Body>
</soap:Envelope>
```

## 7. Kubernetes deployment

Build and push the image:

```bash
mvn clean package
docker build -t your-registry/country-info-microservice:1.0.0 .
docker push your-registry/country-info-microservice:1.0.0
```

Change the image in `k8s/deployment.yaml` to your registry image.

Apply:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/hpa.yaml
kubectl apply -f k8s/ingress.yaml
```

Verify:

```bash
kubectl get pods -n country-info
kubectl get svc -n country-info
kubectl get hpa -n country-info
kubectl rollout status deployment/country-info-api -n country-info
```

For a quick local test without Ingress:

```bash
kubectl port-forward -n country-info svc/country-info-api 8080:80
```

Then call the REST API on localhost:8080.

### Production note

`k8s/secret.yaml` contains placeholder credentials intentionally. Do not commit real passwords. Use a Kubernetes Secret manager, sealed secrets, External Secrets, or your cloud provider's secret store.

For production, MySQL should normally be an externally managed HA database rather than a single in-cluster StatefulSet.

## 8. Troubleshooting Kubernetes

### Pods are Pending

```bash
kubectl describe pod <pod> -n country-info
kubectl get events -n country-info --sort-by=.lastTimestamp
```

Look for insufficient CPU/memory, image-pull problems, or scheduling constraints.

### ImagePullBackOff

```bash
kubectl describe pod <pod> -n country-info
```

Confirm the image exists and, for private registries, configure `imagePullSecrets`.

### CrashLoopBackOff

```bash
kubectl logs <pod> -n country-info --previous
kubectl describe pod <pod> -n country-info
```

Check database configuration, environment variables and startup exceptions.

### Readiness probe failing

```bash
kubectl exec -n country-info deploy/country-info-api -- wget -qO- http://localhost:8080/actuator/health/readiness
```

Confirm the application is listening on 8080 and its dependencies are configured correctly.

### Database connectivity

```bash
kubectl logs statefulset/mysql -n country-info
kubectl exec -n country-info <mysql-pod> -- mysqladmin ping -h 127.0.0.1 -uroot -p
```

Also verify:

```bash
kubectl get secret country-info-secret -n country-info
kubectl get configmap country-info-config -n country-info -o yaml
```

### SOAP service failures

Inspect application logs:

```bash
kubectl logs deployment/country-info-api -n country-info --tail=200
```

The SOAP client uses bounded retries and then opens its circuit after repeated failures. This prevents every application pod from continuously hammering an unavailable external dependency.

## 9. High-load design decisions

- **Stateless API:** no in-memory user/session state; horizontal scaling is safe.
- **Database as source of truth:** all persisted country information is stored in MySQL.
- **Connection pooling:** supplied by the Spring Boot datasource stack.
- **HPA:** scales API pods from 3 to 10 based on CPU.
- **Readiness/liveness:** traffic is only sent to healthy application pods.
- **Rolling updates:** zero desired unavailable pods during normal rollout.
- **SOAP resilience:** bounded timeout, retry/backoff and circuit breaking.
- **Observability:** Actuator health and metrics plus structured application logs.
- **Separation of concerns:** Controller -> Service -> Integration/Repository -> external system/database.

## 10. Trade-offs

A generated SOAP client from the WSDL would provide stronger compile-time typing, but it adds generated source/build complexity. This implementation uses a small dedicated SOAP client so the assessment can be cloned and run with a standard Maven build while keeping the SOAP boundary isolated.

The circuit breaker is intentionally lightweight and in-process. For a large production platform, a shared resilience library such as Resilience4j plus distributed tracing would be preferable.

The SOAP provider is an external dependency, so imported data should be treated as external data and validated before persistence.

## 11. GitHub submission

```bash
git init
git add .
git commit -m "Implement country information integration microservice"
git branch -M main
git remote add origin https://github.com/<your-user>/country-info-microservice.git
git push -u origin main
```

Before submission:

1. Replace placeholder Docker registry image.
2. Replace Kubernetes secrets with secure values outside Git.
3. Run `mvn clean test`.
4. Run the Docker Compose integration manually.
5. Test POST, GET, PUT and DELETE.
6. Run `kubectl rollout status`.
7. Share the GitHub repository URL.

## 12. Assessment mapping

| Requirement | Implementation |
|---|---|
| Spring Boot setup | `pom.xml`, Java 21 |
| Spring Web/JPA/MySQL | Maven dependencies |
| SoapUI/WSDL | README SOAP verification |
| POST country name | `POST /api/v1/countries/import` |
| Sentence case | `CountryInfoService.sentenceCase()` |
| CountryISOCode | `CountrySoapClient.countryIsoCode()` |
| FullCountryInfo | `CountrySoapClient.fullCountryInfo()` |
| CountryInfo + Language models | JPA entities |
| CRUD | Controller + Service |
| Kubernetes scripts | `k8s/` |
| Deployment guide | README |
| Troubleshooting guide | README |
| Stateless/high load | replicas + HPA |
| Failure handling | timeout + retries + circuit |
| Logging/metrics | SLF4J + Actuator |
| MVC/separation | controller/service/repository/integration |
| Production deployment | Docker + probes + rolling update |
