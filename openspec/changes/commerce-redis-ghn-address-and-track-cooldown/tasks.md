## 1. Redis wiring (commerce)

- [ ] 1.1 Add commerce `RedisConfig` (or Boot `StringRedisTemplate` bean) for string key/value Redis access
- [ ] 1.2 Confirm `spring.data.redis` env works in local docker; document keys prefix `commerce:ghn:*` briefly in commerce README if needed

## 2. GHN address master-data cache

- [ ] 2.1 Implement cache-aside around `GhnAddressMasterDataGateway` (provinces / districts / wards) using keys `commerce:ghn:provinces`, `commerce:ghn:districts:{id}`, `commerce:ghn:wards:{id}` with ~7d TTL
- [ ] 2.2 On Redis errors: log WARN, call GHN (fail-open), do not fail the HTTP request solely due to Redis
- [ ] 2.3 Unit tests: cache hit skips GHN client; miss calls GHN and writes cache; Redis exception falls back to GHN

## 3. Track sync cooldown Redis

- [ ] 3.1 Rewrite `GhnTrackSyncCooldownRegistry` to use Redis key `commerce:ghn:track-cooldown:{shipmentId}` with TTL = `trackSyncCooldownSeconds`
- [ ] 3.2 Preserve `force` bypass and cooldownSeconds ≤ 0 → always sync; Redis errors → allow sync (fail-open) + WARN
- [ ] 3.3 Unit tests: key present blocks non-force sync; markSynced sets TTL; force ignores key; Redis failure allows sync

## 4. Verify

- [ ] 4.1 Run commerce-service unit tests for address gateway/cooldown paths
- [ ] 4.2 Manual smoke (optional): open address form twice → second load should not hit GHN if Redis up; track sync twice within cooldown → second non-force skipped
