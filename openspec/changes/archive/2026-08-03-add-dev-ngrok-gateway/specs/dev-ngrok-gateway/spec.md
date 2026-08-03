## ADDED Requirements

### Requirement: Single HTTPS entry for remote dev demo

The dev ngrok gateway SHALL expose all backend APIs, frontend SPA, and MinIO object paths through one public HTTPS origin via a single ngrok tunnel to a reverse proxy.

#### Scenario: One public URL for all clients

- **WHEN** the 
grok compose profile is active and the ngrok tunnel is online
- **THEN** web, mobile, and external webhooks MUST be able to reach services using the same gateway host (e.g. https://{tunnel-host}/...)

#### Scenario: Local dev without ngrok unchanged

- **WHEN** the 
grok compose profile is not started
- **THEN** existing localhost and LAN IP development workflows MUST continue to work without gateway containers running

### Requirement: Path-based API routing

The dev gateway SHALL route HTTP requests to backend services by path prefix without modifying request paths.

#### Scenario: Commerce API routing

- **WHEN** a request arrives at {gateway}/commerce/api/v1/products
- **THEN** the gateway MUST forward the request to commerce-service with path /commerce/api/v1/products unchanged

#### Scenario: Auth API routing

- **WHEN** a request arrives at {gateway}/api/v1/auth/login
- **THEN** the gateway MUST forward to auth-service with path /api/v1/auth/login unchanged

#### Scenario: Auth OAuth2 authorization routing

- **WHEN** a browser navigates to {gateway}/oauth2/authorization/{provider}
- **THEN** the gateway MUST forward to auth-service unchanged
- **AND** MUST NOT serve the frontend SPA for that path

#### Scenario: Auth OAuth2 callback routing

- **WHEN** an identity provider redirects to {gateway}/login/oauth2/code/{provider}
- **THEN** the gateway MUST forward to auth-service unchanged
- **AND** MUST NOT serve the frontend SPA for that path

#### Scenario: Social API routing

- **WHEN** a request arrives at {gateway}/api/v1/social/feed/global
- **THEN** the gateway MUST forward to social-service unchanged

#### Scenario: Admin API routing

- **WHEN** a request arrives at {gateway}/admin/api/v1/auth/login
- **THEN** the gateway MUST forward to admin-service unchanged

#### Scenario: Notification API routing

- **WHEN** a request arrives at {gateway}/api/v1/notification/notifications
- **THEN** the gateway MUST forward to notification-service unchanged

#### Scenario: Routing precedence

- **WHEN** a request path matches both /api/v1/social/ and the generic /api/v1/ auth fallback
- **THEN** the gateway MUST route to social-service, not auth-service

### Requirement: Frontend SPA routing without API conflict

The dev gateway SHALL serve the frontend SPA for non-API paths including /commerce/* and /admin/* that are not API prefixes.

#### Scenario: Commerce checkout page

- **WHEN** a browser requests {gateway}/commerce/checkout
- **THEN** the gateway MUST serve the frontend SPA (not commerce-service API)

#### Scenario: Admin dashboard page

- **WHEN** a browser requests {gateway}/admin/commerce/shops
- **THEN** the gateway MUST serve the frontend SPA (not admin-service API)

#### Scenario: SPA deep link fallback

- **WHEN** a browser requests an unknown path under / that does not match API or MinIO routes
- **THEN** the gateway MUST return index.html for client-side routing

### Requirement: MinIO object routing through gateway

The dev gateway SHALL proxy MinIO bucket paths at the URL root for configured 2Hands buckets.

#### Scenario: Avatar object GET

- **WHEN** a client requests {gateway}/2hands-avatar/{object-key}
- **THEN** the gateway MUST proxy to MinIO port 9000 with the same path

#### Scenario: Social post media GET

- **WHEN** a client requests {gateway}/2hands-social-post/{object-key}
- **THEN** the gateway MUST proxy to MinIO unchanged

#### Scenario: Presigned PUT upload

- **WHEN** a client performs HTTP PUT to a presigned URL whose host is the gateway and path starts with a configured bucket prefix
- **THEN** the gateway MUST forward the request to MinIO and MUST preserve the Host header expected by the presigned signature

### Requirement: PayOS webhook via gateway

The gateway SHALL forward PayOS webhook requests to commerce-service without a separate ngrok tunnel.

#### Scenario: PayOS webhook URL

- **WHEN** PayOS sends POST to {gateway}/commerce/api/v1/payments/webhooks/payos
- **THEN** commerce-service MUST receive and process the webhook as it does for direct port 3003 access

### Requirement: Forwarded headers for HTTPS semantics

The dev gateway SHALL set X-Forwarded-Proto, X-Forwarded-Host, and Host headers so backends and OAuth validation see the public HTTPS origin.

#### Scenario: OAuth web callback validation

- **WHEN** auth-service validates redirect URI {gateway}/oauth/success against CORS_ALLOWED_ORIGINS
- **THEN** the request as seen by auth-service MUST reflect scheme https and host matching the ngrok gateway

#### Scenario: OAuth IdP callback base URL behind gateway

- **WHEN** a browser starts OAuth at {gateway}/oauth2/authorization/{provider}
- **THEN** auth-service MUST register the IdP callback as {gateway}/login/oauth2/code/{provider} (HTTPS, public host)
- **AND** MUST use forwarded headers from the gateway (X-Forwarded-Proto, Host) when building OAuth2 `{baseUrl}`

### Requirement: Remote demo environment documentation

The repository SHALL provide documented environment variables and scripts to obtain the current ngrok gateway URL and derived configuration checklist.

#### Scenario: Print gateway URL after tunnel start

- **WHEN** an operator runs the provided print script after ngrok is online
- **THEN** the script MUST output the HTTPS gateway URL, PayOS webhook URL, and required env keys for FE, mobile, and MinIO presign

#### Scenario: Env example template

- **WHEN** an operator sets up remote demo for the first time
- **THEN** Infrastructure/.env.ngrok.example MUST list NGROK_AUTHTOKEN, gateway-related MinIO presign variables, and CORS/OAuth overrides

### Requirement: Client upload origin on public gateway host

Frontend and mobile clients SHALL NOT send an invalid client_upload_origin when using a public ngrok gateway host.

#### Scenario: Web upload on ngrok host

- **WHEN** the web app runs on a public gateway hostname (not localhost or private LAN)
- **THEN** getClientUploadOrigin() MUST NOT return http://{host}:9000
- **AND** upload-url requests MUST omit client_upload_origin so the server uses *_MINIO_PRESIGNED_ENDPOINT

#### Scenario: Mobile upload on ngrok host

- **WHEN** mobile EXPO_PUBLIC_*_BASE_URL points to the public gateway HTTPS origin
- **THEN** mobile MUST omit client_upload_origin on presigned upload-url requests (same rule as web)

#### Scenario: LAN dev upload unchanged

- **WHEN** mobile uses a private LAN host (e.g. 10.x.x.x) for dev
- **THEN** existing client_upload_origin behavior with http://{lan-host}:9000 MUST remain supported

### Requirement: Frontend build for remote demo

Remote web demo SHALL use a production frontend build with all VITE_*_SERVICE_BASE_URL values set to the gateway HTTPS origin.

#### Scenario: API calls from built frontend

- **WHEN** the production build is served from the gateway root
- **THEN** API clients MUST call {gateway}/api/v1/..., {gateway}/commerce/api/v1/..., etc., using the configured base URL equal to the gateway origin
