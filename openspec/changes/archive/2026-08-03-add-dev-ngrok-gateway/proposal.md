## Why

Local development today requires every client (web, mobile, PayOS webhooks) to reach services via localhost or LAN IP (10.x.x.x), which blocks remote demos, mobile testing over 4G, and external webhook callbacks. The repo already has a single-service ngrok tunnel for PayOS (docker-compose.payos.yml), but exposing the full stack (5 backends, frontend, MinIO) needs seven separate tunnels or a unified approach. A single **dev gateway + one ngrok tunnel** matches production path routing, works on ngrok free tier, and enables full remote E2E (web demo, mobile, image upload).

## What Changes

- Add Infrastructure/nginx/dev-gateway.conf and docker-compose.ngrok.yml with compose profile 
grok (dev-gateway + ngrok agent).
- Route all public traffic through one HTTPS URL by path prefix: API services, MinIO buckets (/2hands-*), and frontend SPA.
- Add orchestration scripts (start-ngrok-demo, print-ngrok-urls, optional env patch) under Infrastructure/scripts/.
- Document B1 remote-demo workflow in Infrastructure/README.md (env matrix, rebuild FE when ngrok URL changes, verification checklist).
- Adjust frontend/mobile getClientUploadOrigin to omit client_upload_origin on public (ngrok) hosts so presigned uploads use server-configured *_MINIO_PRESIGNED_ENDPOINT.
- Provide .env.ngrok.example templates for gateway URL, MinIO presign endpoints, OAuth/CORS, and PayOS return URLs.
- Deprecate standalone PayOS-only ngrok flow in favor of the unified gateway (keep docker-compose.payos.yml documented as legacy until removed).

## Capabilities

### New Capabilities

- dev-ngrok-gateway: Single reverse-proxy dev gateway exposing all backend services, frontend (production build), and MinIO through one ngrok HTTPS URL with path-based routing and operational scripts.

### Modified Capabilities

_(none - no existing OpenSpec capability specs in openspec/specs/)_

## Impact

- **Infrastructure**: new nginx config, compose overlay, scripts, README section; extends existing NGROK_AUTHTOKEN in Infrastructure/.env.
- **Frontend**: getClientUploadOrigin.js; build-time VITE_*_SERVICE_BASE_URL must point to gateway URL for remote demo; no API path changes.
- **Mobile**: .env / getClientUploadOrigin.js for ngrok base URL and upload behavior; 
esolveDevServiceBaseUrl already supports explicit env URLs.
- **Backend services** (auth, social, commerce): runtime env for CORS_ALLOWED_ORIGINS, OAuth redirect URLs, *_MINIO_PRESIGNED_ENDPOINT, *_MINIO_PUBLIC_URL, PayOS return URLs when profile 
grok is active - no controller or API contract changes.
- **Service-to-service traffic**: unchanged (Docker internal hostnames).
- **Dependencies**: ngrok account + authtoken; optional ngrok paid reserved domain to avoid FE rebuild on URL rotation.
