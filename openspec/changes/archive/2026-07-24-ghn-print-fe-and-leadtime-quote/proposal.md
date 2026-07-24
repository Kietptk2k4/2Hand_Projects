## Why

Checkout already shows “Dự kiến giao” but the ETA is a local heuristic (`SAME_DAY` / `EXPRESS` / `STANDARD` day offsets), not GHN’s leadtime. After creating a GHN shipment, Create Order returns `expected_delivery_time` that we ignore, so stored `estimated_delivery_date` stays inaccurate. Seller can print labels via BE (`GET .../ghn/print-label`) but FE has no action, so ops still leave the app to print.

## What Changes

- Call GHN **Calculate expected delivery time** (`/shipping-order/leadtime`) alongside Calculate Fee when quoting shipping for checkout; on leadtime failure keep fee and fall back to the existing heuristic ETA.
- Parse GHN Create Order `expected_delivery_time` and update `shipments.estimated_delivery_date` when registering a GHN shipment (override the heuristic insert).
- Wire seller FE **In vận đơn**: call existing print-label API, default format A5, open `print_url` in a new tab.
- **Out of scope:** GHN Return Order API / seller “yêu cầu hoàn hàng”; ETA refresh from Order Info sync; mobile print UI.

## Capabilities

### New Capabilities

- `ghn-shipping-eta`: GHN-backed estimated delivery date on fee quote (leadtime API + heuristic fallback) and on create shipment (parse Create Order `expected_delivery_time`).
- `seller-ghn-print-label-fe`: Seller web UI to generate and open a GHN print label for an existing GHN shipment.

### Modified Capabilities

- (none — no existing commerce/GHN capability specs under `openspec/specs/`)

## Impact

- **commerce-service:** `ShippingFeeQuoteService`, new `GhnLeadtimeGateway` (+ adapter), `GhnCreateOrderResult` / `GhnShipmentGatewayAdapter.parseCreateResponse`, `updateGhnProviderFields` (also update ETA), unit tests; README / api-fe-behavior docs as needed.
- **frontend (commerce seller):** `sellerShipmentApi`, `useSellerShipmentDetail`, `CommerceSellerShipmentDetailPage` print section.
- **External:** GHN leadtime API (token + shop_id); reuse existing print gen-token flow.
- **Not impacted:** Return Order, webhook return mapping (unchanged), PayOS, admin override.
