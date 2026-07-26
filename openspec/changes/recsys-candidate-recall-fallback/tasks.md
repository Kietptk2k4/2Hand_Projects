## 1. Config and API surface

- [x] 1.1 Add `social.recommendation.recall.min-pool-size` (default 20) and `social.recommendation.recall.window-days` (default `7,30,90`) to `application.yml` / env examples
- [x] 1.2 Wire config into `CandidatePoolServiceImpl` (or a small recall helper) without changing `CandidatePoolService` callers’ maxSize contract

## 2. Progressive recall implementation

- [x] 2.1 Refactor pool build to accept a window days parameter (same ACTIVE/PUBLIC/moderation/followee/public/seen rules)
- [x] 2.2 Implement ladder: try each window until candidate count after filters `>= N`, else use widest window result
- [x] 2.3 Log chosen window days and resulting pool size at INFO/DEBUG for ops diagnosis
- [x] 2.4 Confirm `PostFeatureBuilder` recency half-life remains 7d (no code change unless drift found)

## 3. Tests

- [x] 3.1 Unit test: 7d pool `>= N` → no wider query / returns 7d set
- [x] 3.2 Unit test: 7d `&lt; N`, 30d `>= N` → uses 30d
- [x] 3.3 Unit test: 7d and 30d `&lt; N` → uses 90d (including empty)
- [x] 3.4 Unit test: seen posts excluded even when only visible under wider window

## 4. Verify

- [x] 4.1 Restart Social; call `/feed/for-you` for a user with older unseen posts → non-empty when 90d inventory exists
- [x] 4.2 Confirm empty only when no unseen eligible posts in 90d (not ONNX load failure)
