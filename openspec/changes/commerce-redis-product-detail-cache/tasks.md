## 1. Redis cache port

- [ ] 1.1 Ensure commerce Redis wiring exists (`StringRedisTemplate` / `RedisConfig`) — reuse GHN Sprint A config if present, else add minimal config
- [ ] 1.2 Add `ProductDetailCache` port + Redis adapter: `get(productId)`, `put(productId, detail, ttl)`, `evict(productId)`, `evictAll(productIds)` with fail-open WARN
- [ ] 1.3 Add config `commerce.cache.product-detail-ttl-seconds` (default 60)

## 2. Read path

- [ ] 2.1 Wire cache-aside in `ViewProductDetailUseCase` (or thin application helper): GET → miss → repository → PUT
- [ ] 2.2 Unit tests: hit skips repository; miss loads and puts; Redis error still returns DB detail

## 3. After-commit invalidation (product-scoped)

- [ ] 3.1 Evict after commit on: update product, media, attributes, price, inventory; publish/pause/archive/remove/restore (seller + admin restore/remove)
- [ ] 3.2 Evict affected product ids after inventory reserve and release paths that change stock
- [ ] 3.3 Unit or focused tests: at least one price update and one inventory/reserve path call evict after success

## 4. Shop-wide invalidation

- [ ] 4.1 On shop vacation update (and shop profile update if PDP embeds shop name/avatar): resolve product ids for shop and `evictAll`
- [ ] 4.2 Prefer after-commit; fail-open if Redis/list fails (log WARN)

## 5. Verify

- [ ] 5.1 Run commerce unit tests for view product detail + touched write use cases
- [ ] 5.2 Manual smoke: open PDP twice (second hit cache); change price → next PDP shows new price; confirm add-to-cart still works from DB path
