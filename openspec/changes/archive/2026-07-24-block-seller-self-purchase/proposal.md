## Why

In 2Hands C2C, `seller_id` on shop/product is the same UUID as the Auth user. Sellers can currently add their own listings to cart and complete checkout, which is meaningless for a closet marketplace, wastes inventory on 1-of-1 items, and creates buyer=seller orders. FR/spec never forbade this, and `AddProductToCartUseCase` / checkout prepare only validate purchasability (ACTIVE, stock, price)—not buyer ≠ seller.

## What Changes

- Reject add-to-cart when the authenticated user is the product’s `seller_id`.
- Reject checkout (and order-total quote if it would proceed) when any selected cart line belongs to the buyer as seller.
- Introduce a clear error code (e.g. `COMMERCE-409-SELF-PURCHASE`) with a user-facing message.
- Soft UX: hide/disable buy CTA on own product pages in web FE (and map the API error).
- **Not in scope:** auto-purging historical self-cart items (checkout reject is enough); admin impersonation; changing shop ownership model.

## Capabilities

### New Capabilities

- `seller-self-purchase-guard`: Prevent a user from purchasing their own product listings via cart and checkout.

### Modified Capabilities

- (none — no existing commerce capability specs under `openspec/specs/`)

## Impact

- **commerce-service:** `AddProductToCartUseCase`, checkout prepare / `CheckoutFromCartUseCase` (and possibly `CalculateOrderTotalUseCase`), domain policy helper, `ErrorCode`, unit tests, api-fe-behavior docs.
- **frontend:** product detail buy CTA gating; cart/checkout error mapping.
- **mobile (optional same sprint):** same CTA gate if shared patterns exist; otherwise follow-up.
