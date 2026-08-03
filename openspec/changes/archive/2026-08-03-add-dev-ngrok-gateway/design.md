## Context

2Hands local dev runs five Spring Boot services on ports 3001-3005, Vite frontend on 5173, and MinIO on 9000 (Docker `Infrastructure/docker-compose.dev.yml`). Clients use per-service base URLs (`http://localhost:3001`, etc.). API paths are already disambiguated in controllers:

| Service | API path prefix | FE routes (no conflict) |
|---------|-----------------|-------------------------|
| auth | `/api/v1/auth`, `/api/v1/users` | `/auth/login`, `/oauth/success` |
| social | `/api/v1/social/` | `/social/` |
| commerce | `/commerce/api/v1/` | `/commerce/checkout`, `/commerce/products` |
| admin | `/admin/api/v1/` | `/admin/`, `/admin/commerce/` |
| notification | `/api/v1/notification/` | _(no FE prefix overlap)_ |
| MinIO | `/2hands-avatar/`, `/2hands-social-post/`, ... | - |

Production targets `https://api.2hands.vn/{service}/api/v1/...`; dev uses ports instead. PayOS webhook today uses `docker-compose.payos.yml` tunneling only commerce `:3003`.

Constraints: ngrok free tier allows one agent session with limited tunnels; presigned MinIO URLs must match the host clients use; `CommerceClientUploadOriginValidator` only allows private/LAN hosts for `client_upload_origin`; Vite bakes `VITE_*` at build time.

## Goals / Non-Goals

**Goals:**

- Expose web demo, mobile (4G), MinIO uploads, and PayOS/VNPAY webhooks through **one** public HTTPS URL.
- Path-based reverse proxy (nginx) on Docker network; single ngrok tunnel to gateway port.
- Same-origin for browser (FE + API + MinIO media on one host).
- Opt-in via compose profile `ngrok`; local daily dev unchanged without the profile.
- Scripts to discover ngrok URL, print env checklist, and orchestrate demo startup.

**Non-Goals:**

- Production API gateway deployment (only local dev / demo).
- Vite HMR through ngrok (use production FE build for remote demo).
- Replacing LAN IP workflow for same-WiFi mobile dev.
- Changing backend API contracts, controller paths, or service-to-service Docker DNS.
- ngrok paid features (reserved domains) - documented as optional improvement only.

## Decisions

### 1. nginx dev-gateway as single entry point

**Choice:** Add `dev-gateway` container (nginx) listening on `:8080`, upstream to Docker service names.

**Alternatives:** Caddy (simpler config but new pattern in repo); Traefik (overkill); multiple ngrok tunnels (exceeds free tier).

**Rationale:** Matches existing `frontend/nginx/default.conf` pattern; explicit location order for path conflicts.

### 2. Routing order (most specific first)

`
/commerce/api/     -> commerce-service:3003
/admin/api/        -> admin-service:3004
/api/v1/social/    -> social-service:3002
/api/v1/notification/ -> notification-service:3005
~ ^/2hands-(avatar|social-post|commerce-product|commerce-review|commerce-shop)/ -> minio:9000
/api/v1/           -> auth-service:3001
/oauth2/           -> auth-service:3001 (Spring Security OAuth2 start)
/login/oauth2/     -> auth-service:3001 (IdP callback)
/                  -> static FE dist (SPA try_files)
`

**Rationale:** FE routes `/commerce/*` and `/admin/*` do not overlap API prefixes `/commerce/api/` and `/admin/api/`.

### 3. Frontend: production build served by gateway

**Choice:** Build `frontend` with all `VITE_*_SERVICE_BASE_URL` set to gateway ngrok URL; mount `dist/` into gateway nginx root.

**Alternatives:** Vite dev behind proxy (HMR/WebSocket complexity); runtime env injection (not supported by Vite without code change).

**Rationale:** Stable demo; script rebuilds FE when ngrok URL rotates on free tier.

### 4. MinIO presign: server-driven endpoint, omit client_upload_origin on public hosts

**Choice:** Set `*_MINIO_PRESIGNED_ENDPOINT` and `*_MINIO_PUBLIC_URL` to `https://{gateway-host}` (and bucket path for public URL). Update FE/mobile `getClientUploadOrigin()` to return `undefined` when hostname is not localhost/private LAN (i.e. ngrok public host).

**Alternatives:** Extend `isAllowedDevUploadHost` to allow ngrok host (more backend changes); separate MinIO ngrok tunnel (second tunnel).

**Rationale:** `MinioPresignEndpointResolver` already supports `presigned-endpoint` override; avoids validator rejecting public hosts.

### 5. Proxy headers for OAuth and MinIO

**Choice:** Set on all upstream locations:

- `Host $host`
- `X-Forwarded-Proto: https` when behind ngrok (gateway receives HTTP from ngrok agent)
- `X-Forwarded-Host $host`

On `/oauth2/` and `/login/oauth2/`, also set larger `proxy_buffer_*` (OAuth 302 `Location` headers can exceed nginx defaults and cause 502).

**Choice:** auth-service `server.forward-headers-strategy: framework` so Spring Security OAuth2 `{baseUrl}` resolves to `https://{ngrok-host}` (IdP callback `{gateway}/login/oauth2/code/{provider}`).

**Rationale:** `OAuthRedirectUriValidator.sameAuthority`, presigned URL signing, and Google/Facebook redirect URI registration depend on correct public host/scheme.

### 6. Compose profile `ngrok`

**Choice:** `docker-compose.ngrok.yml` adds `dev-gateway` + `ngrok` services; ngrok command `http dev-gateway:8080`. Reuse `NGROK_AUTHTOKEN` from `Infrastructure/.env`. Expose `4040` for tunnel API (scripts).

### 7. Env management

**Choice:** `Infrastructure/.env.ngrok.example` documents variables; `print-ngrok-urls.ps1` / `.sh` reads `http://127.0.0.1:4040/api/tunnels` and prints gateway URL + derived webhook/OAuth/env checklist.

### 8. Deprecate PayOS-only ngrok overlay

**Choice:** Document unified gateway as replacement; keep `docker-compose.payos.yml` until one release cycle.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| ngrok URL rotation breaks baked Vite env | print-ngrok-urls + rebuild script; document reserved domain for paid |
| client_upload_origin with http://ngrok:9000 breaks upload | Omit origin on public host; server presign via env |
| Large uploads (50MB video) over ngrok bandwidth | Document limit; test images first in demo |
| X-Forwarded-Proto wrong -> OAuth mobile bridge fails | Explicit proto header in nginx; manual verify in checklist |
| Gateway catch-all serves stale SPA for API typos | Specific API locations first |
| Free ngrok interstitial on HTML | Accept for browser demo; API/mobile JSON unaffected |

## Migration Plan

1. Implement gateway + scripts behind profile `ngrok` (no change to default `dev` profile).
2. Update `Infrastructure/README.md` with B1 workflow.
3. Mark PayOS ngrok section as superseded by gateway.
4. Rollback: `docker compose --profile ngrok down`; revert client env to localhost/LAN.

## Open Questions

- Whether to add `patch-ngrok-env.ps1` auto-patcher in v1 or only print checklist (recommend print-only v1).
- Whether gateway serves FE from volume populated by host `npm run build` or a compose build service (recommend host/script build for v1).
