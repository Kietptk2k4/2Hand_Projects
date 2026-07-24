## 1. GHN leadtime gateway (BE)

- [x] 1.1 Add domain `GhnLeadtimeGateway` + query/result types (route districts/wards, service_id → LocalDate / raw)
- [x] 1.2 Implement `GhnLeadtimeGatewayAdapter` calling `POST /shiip/public-api/v2/shipping-order/leadtime` with Token + ShopId
- [x] 1.3 Parse `data.leadtime` (unix seconds) to `LocalDate` via existing `Clock`; unit-test adapter parse success/failure

## 2. Fee quote ETA from leadtime (BE)

- [x] 2.1 Wire leadtime into `ShippingFeeQuoteService.quoteViaGhn` (fee then leadtime on request thread; same resolved service)
- [x] 2.2 On leadtime failure/empty: keep fee, use `ShippingDeliveryEstimator`, log WARN
- [x] 2.3 Update `ShippingFeeQuoteServiceTest` / related unit tests for success + leadtime-fallback scenarios

## 3. Create Order expected delivery time (BE)

- [x] 3.1 Extend `GhnCreateOrderResult` with optional `LocalDate expectedDeliveryDate`
- [x] 3.2 Parse `expected_delivery_time` in `GhnShipmentGatewayAdapter.parseCreateResponse` (ISO datetime and date-only); assert in existing adapter test
- [x] 3.3 Update `updateGhnProviderFields` SQL to set `estimated_delivery_date` when ETA present
- [x] 3.4 Return updated ETA from `CreateShipmentUseCase.registerGhnShipment` / `CreateShipmentResult`; cover with unit test

## 4. Seller print label FE

- [x] 4.1 Add `fetchGhnPrintLabel(shipmentId, format)` to `sellerShipmentApi.js`
- [x] 4.2 Extend `useSellerShipmentDetail` with print action (loading/error state)
- [x] 4.3 Add “In vận đơn” section on `CommerceSellerShipmentDetailPage` (GHN + `ghnOrderCode`; default A5; optional format; `window.open(print_url)`)
- [x] 4.4 Map print API errors via seller shipment error constants if needed

## 5. Docs & verify

- [x] 5.1 Update `Services/commerce-service/README.md` Sprint note for leadtime quote + create ETA + FE print
- [x] 5.2 Add/update api-fe-behavior docs for Calculate Shipping Fee ETA source and ViewGhnPrintLabel FE usage (per commerce doc rules)
- [x] 5.3 Run commerce-service unit tests for touched GHN/shipping paths; smoke seller print + checkout ETA manually when GHN staging available
