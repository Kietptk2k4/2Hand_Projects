## ADDED Requirements

### Requirement: Seller can open GHN print label from shipment detail

The seller web app SHALL allow a seller to generate a GHN print-label URL for their GHN shipment that already has a GHN order code, using the existing commerce print-label API, and SHALL open that URL for printing.

#### Scenario: Print label with default A5

- **WHEN** a seller views a GHN shipment detail that has a non-empty `ghn_order_code`
- **AND** the seller chooses to print the label without selecting another format
- **THEN** the app calls `GET /commerce/api/v1/seller/shipments/{id}/ghn/print-label` with format `a5` (or default accepted by the API)
- **AND** on success opens the returned `print_url` in a new browser tab

#### Scenario: Print label format selection

- **WHEN** a seller selects format `a5`, `80x80`, or `52x70` and confirms print
- **THEN** the app requests that format from the print-label API
- **AND** opens the returned `print_url` on success

#### Scenario: Print not offered for non-GHN or missing code

- **WHEN** the shipment carrier is not GHN or `ghn_order_code` is missing
- **THEN** the print label action is not available

#### Scenario: Print API failure

- **WHEN** the print-label API returns an error
- **THEN** the app shows an error message to the seller
- **AND** does not navigate away from the shipment detail page
