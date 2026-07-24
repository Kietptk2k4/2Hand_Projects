## Context

2Hands is a C2C closet marketplace: each user may own one seller shop; `products.seller_id` / `seller_shops.seller_id` equal the Auth user UUID. Buyers and sellers share the same account model. Today a logged-in seller can add their own ACTIVE listing to cart and checkout because purchasability checks ignore `buyerId == sellerId`.

Primary gates today:

- `AddProductToCartUseCase.validatePurchasable` — status/price/stock only.
- `CheckoutFromCartRepositoryAdapter.prepareCheckout` — same purchasability, no self-purchase check.
- FE product cards / PDP expose add-to-cart and buy-now without comparing `sellerId` to session user.

## Goals / Non-Goals

**Goals:**

- Hard-block self-purchase at add-to-cart and checkout prepare.
- Dedicated error code for FE messaging.
- Soft-block buy CTAs on web when viewing own listing.
- Unit tests covering add-cart and checkout rejection.

**Non-Goals:**

- Automatically deleting existing self-cart lines (checkout reject covers legacy carts).
- Changing identity model (seller remains user UUID).
- Mobile-only work as a hard requirement (do if cheap; otherwise follow-up).
- Blocking sellers from browsing their own PDP (view is fine; purchase is not).

## Decisions

### D1 — Shared domain policy

Add `SelfPurchasePolicy` (or equivalent) in domain:

```text
assertNotOwnListing(buyerId, sellerId) → throws AppException if equal / null-safe
```

Call from:

1. `AddProductToCartUseCase` after loading `ProductPurchaseContext`.
2. `CheckoutFromCartRepositoryAdapter` (or application layer before reserve) for each line’s `seller_id` vs `request.buyerId()`.
3. Optionally `CalculateOrderTotalUseCase` so quote fails early with the same code (recommended for consistent UX).

**Alternatives:** Only FE hide (bypassable). Only cart (legacy cart still checkoutable). Rejected for defense in depth.

### D2 — Error code `COMMERCE-409-SELF-PURCHASE`

New `ErrorCode.SELF_PURCHASE` → HTTP 409 Conflict (same family as `NOT_PURCHASABLE`). Message: buyer cannot purchase their own product.

**Alternatives:** Reuse `NOT_PURCHASABLE` (less clear for FE). Prefer dedicated code.

### D3 — Checkout: fail entire request if any line is self-owned

Do not silently drop lines. Return 409 so the buyer must remove the item.

### D4 — FE soft gate

Where `product.sellerId === currentUserId` (JWT subject / auth session):

- Hide or disable “Thêm vào giỏ” / “Mua ngay” on PDP and cards when seller id is known.
- Map `COMMERCE-409-SELF-PURCHASE` in cart/checkout error helpers.

API remains source of truth.

## Risks / Trade-offs

- [Legacy self-cart items] → Checkout/quote reject until user removes them; optional later: cart sync marks INVALID.
- [FE without sellerId on some list DTOs] → Rely on API reject; enrich DTO only if needed for CTA hide.
- [Buy-now path] → Usually goes through cart/checkout APIs; ensure both entry points hit the same BE guards.

## Migration Plan

1. Deploy commerce-service with policy + error code (backward compatible for non-self purchases).
2. Deploy FE CTA + error mapping.
3. Rollback: revert BE (self-buy returns) or FE only (API still blocks).

No DB migration.

## Open Questions

- None blocking. Optional follow-up: cart sync status for self-owned lines.
