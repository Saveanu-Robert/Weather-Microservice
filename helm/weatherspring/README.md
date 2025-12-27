# WeatherSpring Helm Chart

Helm chart for deploying WeatherSpring microservice on Kubernetes.

## What's Included

- Horizontal Pod Autoscaler (2-5 replicas based on CPU usage)
- Health probes (startup, liveness, readiness)
- Persistent storage for H2 database
- Network policies for security
- Pod disruption budgets for high availability
- Prometheus metrics (ServiceMonitor support)
- Security contexts (non-root user, read-only filesystem)
- Ingress support

## Prerequisites

- Kubernetes 1.20+
- Helm 3.0+
- Metrics Server (for autoscaling)
- WeatherAPI.com API key (free at https://www.weatherapi.com/)

For Minikube:
```powershell
minikube start --cpus=4 --memory=8192
minikube addons enable ingress
minikube addons enable metrics-server
```

## Quick Start

### 1. Build and Load Image

```powershell
docker build -t weatherspring/weather-service:1.0.0 .
minikube image load weatherspring/weather-service:1.0.0
```

### 2. Install Chart

Start tunnel in a separate window (as Administrator):
```powershell
minikube tunnel
```

Deploy the chart:
```powershell
helm install weatherspring ./helm/weatherspring `
  -f ./helm/weatherspring/values-minikube-windows.yaml `
  --set secrets.weatherApiKey=YOUR_API_KEY
```

### 3. Access Application

```
http://127.0.0.1:8080/swagger-ui.html
```

## Configuration

### Important Values

| Parameter | Description | Default |
|-----------|-------------|---------|
| `image.repository` | Docker image repository | `weatherspring/weather-service` |
| `image.tag` | Image tag | `1.0.0` |
| `image.pullPolicy` | Pull policy | `IfNotPresent` |
| `replicaCount` | Number of replicas | `2` |
| `secrets.weatherApiKey` | WeatherAPI.com API key | Required |
| `service.type` | Service type | `ClusterIP` |
| `service.port` | Service port | `8080` |
| `ingress.enabled` | Enable ingress | `false` |
| `persistence.enabled` | Enable persistent storage | `false` |
| `persistence.size` | PVC size | `1Gi` |
| `autoscaling.enabled` | Enable HPA | `false` |
| `autoscaling.minReplicas` | Min replicas | `2` |
| `autoscaling.maxReplicas` | Max replicas | `5` |
| `autoscaling.targetCPUUtilizationPercentage` | CPU target | `80` |

### Resource Limits

Default resource configuration:

```yaml
resources:
  requests:
    memory: 512Mi
    cpu: 250m
  limits:
    memory: 1Gi
    cpu: 1000m
```

Override in your values file or with `--set`:

```powershell
helm install weatherspring ./helm/weatherspring `
  --set resources.requests.memory=1Gi `
  --set resources.limits.memory=2Gi
```

### Profiles

Spring profiles can be set:

```yaml
springProfiles: "prod"
```

Available profiles:
- `dev` - Development (DEBUG logging)
- `test` - Testing
- `prod` - Production (INFO logging, file-based H2)

### Environment Variables

Add custom environment variables:

```yaml
env:
  - name: CACHE_CURRENT_WEATHER_TTL
    value: "600"
  - name: LOG_LEVEL
    value: "DEBUG"
```

## Values Files

- `values.yaml` - Default configuration
- `values-minikube-windows.yaml` - Minikube using LoadBalancer

## Common Operations

### Upgrade

```powershell
helm upgrade weatherspring ./helm/weatherspring `
  -f ./helm/weatherspring/values-minikube-windows.yaml `
  --reuse-values
```

### Uninstall

```powershell
helm uninstall weatherspring
```

### Check Status

```powershell
helm status weatherspring
kubectl get pods -l app.kubernetes.io/name=weatherspring
kubectl get svc weatherspring
```

### View Logs

```powershell
kubectl logs -l app.kubernetes.io/name=weatherspring --tail=50 -f
```

### Access Swagger UI

```
http://127.0.0.1:8080/swagger-ui.html
```

## Features

### Autoscaling

Horizontal Pod Autoscaler scales replicas based on CPU usage:

```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 5
  targetCPUUtilizationPercentage: 80
```

Check HPA status:
```powershell
kubectl get hpa weatherspring
```

### Health Probes

Three types of probes ensure pod health:

- **Startup probe**: Waits for application to start (up to 5 minutes)
- **Liveness probe**: Restarts pod if application is stuck
- **Readiness probe**: Removes pod from service if not ready

All probes use `/actuator/health` endpoint.

### Persistence

Enable persistent storage for H2 database:

```yaml
persistence:
  enabled: true
  size: 1Gi
  storageClass: ""  # Uses default storage class
```

### Ingress

Enable ingress for external access:

```yaml
ingress:
  enabled: true
  className: nginx
  hosts:
    - host: weather.local
      paths:
        - path: /
          pathType: Prefix
```

Add to `C:\Windows\System32\drivers\etc\hosts`:
```
<minikube-ip> weather.local
```

### Network Policy

Restricts network traffic to the pod:

```yaml
networkPolicy:
  enabled: true
  ingressNamespaceSelector:
    kubernetes.io/metadata.name: default
```

### Security Context

Runs container as non-root user with read-only filesystem:

```yaml
securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop:
      - ALL
  readOnlyRootFilesystem: true
  runAsNonRoot: true
  runAsUser: 1000
```

### Service Monitor

For Prometheus Operator:

```yaml
metrics:
  serviceMonitor:
    enabled: true
    interval: 30s
    scrapeTimeout: 10s
```

Metrics available at `/actuator/prometheus`

## Troubleshooting

**Pods not starting:**
```powershell
kubectl describe pod -l app.kubernetes.io/name=weatherspring
kubectl logs -l app.kubernetes.io/name=weatherspring
```

**Image pull errors:**
```powershell
# Verify image is loaded in Minikube
minikube image ls | Select-String weatherspring

# Reload if needed
minikube image load weatherspring/weather-service:1.0.0
```

**Service not accessible:**
```powershell
# Check service
kubectl get svc weatherspring

# Check endpoints
kubectl get endpoints weatherspring

# Ensure tunnel is running (as Administrator)
minikube tunnel
```

**HPA not working:**
```powershell
# Check metrics server
kubectl get deployment metrics-server -n kube-system

# Enable if needed
minikube addons enable metrics-server

# Wait a few minutes for metrics to appear
kubectl get hpa weatherspring
```

## Advanced Configuration

### Custom Values Example

Create `my-values.yaml`:

```yaml
secrets:
  weatherApiKey: your_api_key_here

replicaCount: 3

resources:
  requests:
    memory: 1Gi
    cpu: 500m
  limits:
    memory: 2Gi
    cpu: 1000m

persistence:
  enabled: true
  size: 5Gi

ingress:
  enabled: true
  hosts:
    - host: weather.example.com
      paths:
        - path: /
          pathType: Prefix

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

springProfiles: "prod"

env:
  - name: CACHE_CURRENT_WEATHER_TTL
    value: "600"
  - name: CACHE_FORECAST_TTL
    value: "7200"
```

Deploy:
```powershell
helm install weatherspring ./helm/weatherspring -f my-values.yaml
```

## Chart Structure

```
helm/weatherspring/
├── Chart.yaml                      # Chart metadata
├── values.yaml                     # Default values
├── values-minikube-windows.yaml    # Minikube values
└── templates/
    ├── deployment.yaml             # Deployment resource
    ├── service.yaml                # Service resource
    ├── hpa.yaml                    # HorizontalPodAutoscaler
    ├── ingress.yaml                # Ingress resource
    ├── networkpolicy.yaml          # NetworkPolicy
    ├── pvc.yaml                    # PersistentVolumeClaim
    ├── poddisruptionbudget.yaml    # PodDisruptionBudget
    ├── servicemonitor.yaml         # ServiceMonitor (Prometheus)
    ├── configmap.yaml              # ConfigMap
    ├── secret.yaml                 # Secret
    ├── serviceaccount.yaml         # ServiceAccount
    ├── _helpers.tpl                # Template helpers
    ├── NOTES.txt                   # Post-install notes
    └── tests/
        └── test-connection.yaml    # Helm test
```
