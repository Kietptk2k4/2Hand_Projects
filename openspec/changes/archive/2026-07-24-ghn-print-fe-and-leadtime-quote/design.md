## Context

Commerce already integrates GHN for fee (`/shipping-order/fee`), create order, cancel, print gen-token, master data, webhook, and Order Info sync (Sprint 1–4). Checkout FE displays `estimated_delivery_date` from `POST /shipping/fee`, but `ShippingFeeQuoteService` fills ETA via `ShippingDeliveryEstimator` only. Create shipment inserts the same heuristic, then `updateGhnProviderFields` updates tracking codes without touching ETA — even though Create Order responses include `expected_delivery_time`. Print label BE exists (`ViewGhnPrintLabelUseCase`); seller detail page has cancel but no print action.

Stakeholders: buyer (checkout ETA), seller (print label + accurate shipment ETA), commerce-service maintainers.

Constraints: Clean Architecture (`delivery → application → domain → infrastructure`); no long GHN calls inside DB write transactions beyond existing create pattern; JWT seller ownership for print; docs in `docs/ghn/`.

## Goals / Non-Goals

**Goals:**

- Use GHN leadtime for checkout fee quotes when GHN live client is configured.
- On leadtime failure, keep successful fee and fall back to heuristic ETA (WARN log).
- Persist Create Order `expected_delivery_time` onto `shipments.estimated_delivery_date` when present.
- Seller web can open a GHN print URL from shipment detail (default A5).

**Non-Goals:**

- GHN Return Order / seller-initiated return.
- Refreshing ETA from Order Info on track/sync.
- Mobile print UI.
- Changing fee calculation, service resolve, or webhook status mapping.
- Guaranteeing ETA accuracy as a contractual SLA (quote remains an estimate).

## Decisions

### D1 — Leadtime beside fee in `ShippingFeeQuoteService.quoteViaGhn`

After `ResolveGhnServiceUseCase` returns `service_id`, call Calculate Fee then Leadtime with the same route/service inputs (sequential on the request thread). Parallel `CompletableFuture` on `ForkJoinPool.commonPool()` was rejected because blocking GHN I/O would starve the common pool under concurrent checkout quotes. Map leadtime unix to `LocalDate` via injected `Clock` zone.

**Alternatives:** Parallel Fee+Leadtime on a dedicated bounded executor (optional later for latency); Preview Order instead of leadtime (heavier payload). Chosen: sequential fee-first + soft leadtime for correctness under load.

### D2 — Leadtime failure → heuristic, not quote failure

Fee success is enough for checkout. Leadtime errors / empty data → `ShippingDeliveryEstimator` + WARN. Same mock-fallback rules as today when fee itself fails.

**Alternatives:** Fail entire quote (harsh for buyers); null ETA (breaks FE that already expects a date). Rejected in favor of graceful degradation.

### D3 — Create-time ETA from Create Order response, not a second Leadtime call

Extend `GhnCreateOrderResult` with optional `LocalDate expectedDeliveryDate`. Parse `data.expected_delivery_time` (ISO-8601 datetime or date-only). In `updateGhnProviderFields`, also `SET estimated_delivery_date = :eta` when non-null; otherwise leave heuristic. Return updated ETA on `CreateShipmentResult`.

**Alternatives:** Call leadtime again at create (redundant); only keep quote ETA forever (stale after GHN assigns real leadtime).

### D4 — Print FE mirrors cancel pattern on seller shipment detail

Add `fetchGhnPrintLabel(shipmentId, format)` → existing BE. Show section when `carrier === GHN` and `ghnOrderCode` present. Default format `a5`; optional format selector (`80x80`, `52x70`). On success `window.open(print_url, "_blank")`. Hide or disable for `CANCELLED`/`RETURNED` if print is useless in practice — default: show whenever `ghnOrderCode` exists (GHN may still allow reprint); if BE fails, show error.

**Alternatives:** Print CTA only after create modal (easy to miss later); proxy PDF through our API (unnecessary — GHN URL is designed for this).

### D5 — No Return in this change

Webhook `return*` → `RETURNED` stays as-is for hotro demo. No `switch-status/return` gateway.

## Risks / Trade-offs

- [Extra GHN RPS / latency on every fee quote] → Parallel calls; leadtime fail soft; consider short cache later (not this sprint).
- [Leadtime unix vs Create Order ISO format mismatch] → Dedicated parsers with unit tests for both shapes; fallback heuristic / skip update on parse fail.
- [Popup blockers on `window.open`] → Open only after user click; if blocked, show copyable `print_url`.
- [ETA at quote ≠ ETA after create] → Expected; create overwrites with GHN create response when available.
- [Multi-seller checkout only shows first group ETA in FE] → Pre-existing; BE still returns per-group ETA; FE polish out of scope unless trivial.

## Migration Plan

1. Deploy commerce-service (backward compatible API shapes: fee response already has `estimated_delivery_date`; print endpoint unchanged).
2. Deploy frontend seller print UI.
3. Rollback: disable GHN or revert; heuristic ETA remains valid path; print FE can be reverted independently.

No DB migration — column `estimated_delivery_date` already exists.

## Open Questions

- None blocking. Optional follow-up: short TTL cache for leadtime by (from/to district, ward, service_id).
