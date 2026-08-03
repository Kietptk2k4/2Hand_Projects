## 1. Infrastructure - nginx dev gateway

- [x] 1.1 Create Infrastructure/nginx/dev-gateway.conf with path routing (commerce/api, admin/api, social, notification, auth fallback, MinIO buckets, SPA catch-all)
- [x] 1.2 Set proxy headers (Host, X-Forwarded-Proto: https, X-Forwarded-Host) and client_max_body_size for MinIO uploads
- [x] 1.3 Add dev-gateway service to Infrastructure/docker-compose.ngrok.yml (nginx image, mount config + FE dist volume, port 8080, depends on app services)
- [x] 1.4 Proxy Spring Security OAuth2 paths `/oauth2/` and `/login/oauth2/` to auth-service (before SPA catch-all; fixes React Router 404 on social login via gateway)
- [x] 1.5 OAuth gateway hardening: nginx `proxy_buffer_*` on `/oauth2/` and `/login/oauth2/` (502 from large IdP redirect headers); auth-service `server.forward-headers-strategy=framework` for correct `{baseUrl}` callback

## 2. Infrastructure - ngrok compose profile

- [x] 2.1 Create Infrastructure/docker-compose.ngrok.yml with profile ngrok: dev-gateway + ngrok agent tunneling to dev-gateway:8080
- [x] 2.2 Expose ngrok inspect API on host port 4040; require NGROK_AUTHTOKEN from Infrastructure/.env
- [x] 2.3 Add Infrastructure/.env.ngrok.example with gateway URL placeholders, MinIO presign vars, CORS/OAuth/PayOS overrides

## 3. Infrastructure - scripts and documentation

- [x] 3.1 Add Infrastructure/scripts/print-ngrok-urls.ps1 and .sh (gateway URL, PayOS webhook, env checklist)
- [x] 3.2 Add Infrastructure/scripts/start-ngrok-demo.ps1 (orchestrate: verify infra, start ngrok profile, print URLs)
- [x] 3.3 Add Infrastructure/scripts/build-frontend-ngrok.ps1 to build FE with VITE_* set from current ngrok URL
- [x] 3.4 Document B1 workflow in Infrastructure/README.md (startup order, env matrix, verification checklist, ngrok URL rotation)
- [x] 3.5 Mark docker-compose.payos.yml section as legacy/superseded by unified gateway

## 4. Frontend - upload origin for public gateway

- [x] 4.1 Update frontend/src/fe-module/shared/utils/getClientUploadOrigin.js to return undefined for public (non-localhost, non-private-LAN) hostnames
- [x] 4.2 Add brief comment or dev doc note: remote demo uses server *_MINIO_PRESIGNED_ENDPOINT, not client_upload_origin

## 5. Mobile - upload origin for public gateway

- [x] 5.1 Update mobile/src/shared/utils/getClientUploadOrigin.js with same public-host omit logic as frontend
- [x] 5.2 Update mobile/.env.example with commented ngrok gateway URL examples (https://... for all EXPO_PUBLIC_*_BASE_URL)

## 6. Backend env templates for ngrok profile

- [x] 6.1 Add or extend .env.docker.local.example snippets in auth/social/commerce for *_MINIO_PRESIGNED_ENDPOINT, *_MINIO_PUBLIC_URL, CORS_ALLOWED_ORIGINS, OAuth redirect URLs
- [x] 6.2 Document commerce PayOS return/cancel URLs and AUTH_OAUTH2_COOKIE_SECURE=true for HTTPS gateway in README env matrix

## 7. Verification

- [x] 7.1 Manual smoke: gateway routes auth login, social feed, commerce product list through single ngrok URL
- [x] 7.2 Manual smoke: FE SPA loads /social, /commerce/checkout; API paths /commerce/api/ hit backend
- [x] 7.3 Manual smoke: MinIO GET /2hands-avatar/... and presigned PUT upload (avatar or post) via gateway
- [x] 7.4 Manual smoke: PayOS webhook path reachable at {gateway}/commerce/api/v1/payments/webhooks/payos
- [x] 7.5 Confirm local dev without --profile ngrok still works on localhost/LAN
- [ ] 7.6 Manual smoke: OAuth web login via gateway — `GET {GATEWAY}/oauth2/authorization/google` redirects to IdP (not SPA 404); callback `{GATEWAY}/login/oauth2/code/google` reaches auth-service; `/oauth/success` + `GET /api/v1/auth/oauth/session` completes login

## 8. Auth — OAuth session pollution fix

- [x] 8.1 JwtAuthenticationFilter: valid Bearer JWT overrides existing OAuth2AuthenticationToken in HTTP session
- [x] 8.2 Invalidate HttpSession after OAuth success/failure redirect and after `/oauth/session` + mobile-complete bootstrap
- [x] 8.3 Frontend: hide session-expired modal on login page mount and after email/OAuth session bootstrap
- [ ] 8.4 Manual smoke: Google OAuth via ngrok → `/users/me` 200; email login after failed OAuth still works without clearing cookies manually

## 9. Media via gateway (read-time rewrite, no DB host bake-in)

- [x] 9.1 `StoredMediaUrlRewriter` + `ResponseBodyStoredMediaUrlAdvice` on auth, social, commerce
- [x] 9.2 `*_MINIO_PUBLIC_URL` + `*_MINIO_PRESIGNED_ENDPOINT` from `{GATEWAY}` in `.env.docker.local`
- [x] 9.3 `apply-ngrok-gateway-env.ps1` — parameterized env sync from ngrok API
- [x] 9.4 FE production `resolveDevMediaUrl` same-origin rewrite; mobile gateway rewrite
- [x] 9.5 Optional `migrate-stored-media-urls.ps1` (parameterized SQL; not required when using read-time rewrite)
- [ ] 9.6 Manual smoke: remote device loads feed/product images and videos via `{GATEWAY}/2hands-*`
