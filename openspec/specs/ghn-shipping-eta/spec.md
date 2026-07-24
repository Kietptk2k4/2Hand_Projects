# ghn-shipping-eta

## Purpose

Use GHN leadtime for shipping fee quote ETA when available, and persist Create Order `expected_delivery_time` onto shipment estimated delivery date.

## Requirements

### Requirement: Fee quote uses GHN leadtime when available

When GHN live client is configured and a shipping fee is calculated via GHN Calculate Fee, the system SHALL also request GHN expected delivery time (leadtime) for the same route and resolved service, and SHALL return the resulting calendar date as `estimated_delivery_date` for that seller group.

#### Scenario: Successful fee and leadtime

- **WHEN** a buyer requests a shipping fee quote with GHN-ready pickup and destination addresses
- **AND** both Calculate Fee and leadtime succeed
- **THEN** the seller group response includes the GHN fee total
- **AND** `estimated_delivery_date` is derived from the GHN leadtime value (not the local day-offset heuristic)

#### Scenario: Leadtime fails but fee succeeds

- **WHEN** Calculate Fee succeeds and the leadtime call fails or returns unusable data
- **THEN** the quote still succeeds with the GHN fee
- **AND** `estimated_delivery_date` is filled using the existing local delivery estimator heuristic
- **AND** the failure is logged at WARN without failing the quote

#### Scenario: GHN disabled or mock fallback path

- **WHEN** GHN is disabled or the quote uses the mock fee calculator path
- **THEN** `estimated_delivery_date` continues to use the local delivery estimator heuristic
- **AND** the system does not require a successful leadtime call

### Requirement: Create shipment persists GHN expected delivery time

When a seller creates a GHN shipment and GHN Create Order returns `expected_delivery_time`, the system SHALL parse it into a calendar date and SHALL persist it on `shipments.estimated_delivery_date`, overriding the heuristic date written at local insert.

#### Scenario: Create Order includes expected delivery time

- **WHEN** GHN Create Order succeeds with a parseable `expected_delivery_time`
- **THEN** `shipments.estimated_delivery_date` is updated to that date
- **AND** the create shipment API response reflects the updated estimated delivery date

#### Scenario: Missing or unparseable expected delivery time

- **WHEN** GHN Create Order succeeds but `expected_delivery_time` is missing or cannot be parsed
- **THEN** the shipment remains with the heuristic `estimated_delivery_date` from local insert
- **AND** GHN order code and tracking fields are still updated normally
