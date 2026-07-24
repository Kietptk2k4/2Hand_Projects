## 1. Domain & error code (BE)

- [x] 1.1 Add `ErrorCode.SELF_PURCHASE` (`COMMERCE-409-SELF-PURCHASE`, HTTP 409)
- [x] 1.2 Add domain `SelfPurchasePolicy.assertNotOwnListing(buyerId, sellerId)` throwing that error
- [x] 1.3 Unit-test policy (equal → throw; different → ok; null-safe)

## 2. Cart & checkout guards (BE)

- [x] 2.1 Call policy in `AddProductToCartUseCase` after loading `ProductPurchaseContext`
- [x] 2.2 Call policy in checkout prepare (`CheckoutFromCartRepositoryAdapter` or use-case) for each line vs buyer id
- [x] 2.3 Call policy in `CalculateOrderTotalUseCase` (or shared loader) so quote fails early
- [x] 2.4 Unit tests: add-to-cart self reject; checkout/quote self reject; happy path other seller

## 3. Frontend

- [x] 3.1 Map `COMMERCE-409-SELF-PURCHASE` to clear Vietnamese message in cart/checkout error helpers
- [x] 3.2 Hide/disable add-to-cart and buy-now when `product.sellerId === currentUserId` (PDP + cards where sellerId available)

## 4. Docs & verify

- [x] 4.1 Update api-fe-behavior for AddProductToCart / CheckoutFromCart (and CalculateOrderTotal if needed)
- [x] 4.2 Run commerce-service unit tests for touched cart/checkout paths
