# seller-self-purchase-guard

## Purpose

Prevent sellers from purchasing their own listings at cart, checkout, and quote boundaries, with matching web client soft-gates and error messaging.

## Requirements

### Requirement: Buyer cannot add own listing to cart

The system SHALL reject adding a product to the authenticated user’s cart when the product’s seller id equals the authenticated user id.

#### Scenario: Add own product to cart

- **WHEN** a user calls add-to-cart for a product whose `seller_id` equals their user id
- **THEN** the system returns conflict with code `COMMERCE-409-SELF-PURCHASE`
- **AND** no cart item is created or quantity-increased for that product

#### Scenario: Add another seller’s product

- **WHEN** a user adds a purchasable product whose `seller_id` differs from their user id
- **THEN** add-to-cart proceeds under existing purchasability and stock rules

### Requirement: Buyer cannot checkout own listings

The system SHALL reject checkout (and order-total quote when recalculating selected cart items) when any selected cart line’s `seller_id` equals the buyer id.

#### Scenario: Checkout includes own product

- **WHEN** a buyer attempts checkout with at least one cart item whose product seller is the buyer
- **THEN** the system returns conflict with code `COMMERCE-409-SELF-PURCHASE`
- **AND** no inventory reservation or order is created

#### Scenario: Checkout only other sellers’ products

- **WHEN** all selected cart items belong to sellers other than the buyer
- **THEN** checkout proceeds under existing checkout rules

### Requirement: Client surfaces self-purchase restriction

The web commerce client SHALL not present an enabled purchase CTA (add-to-cart / buy-now) for a product the current user owns when seller identity is available, and SHALL show a clear message when the API returns `COMMERCE-409-SELF-PURCHASE`.

#### Scenario: Own product page without buy CTA

- **WHEN** the current user views a product detail or card where `seller_id` equals their user id
- **THEN** add-to-cart and buy-now actions are hidden or disabled

#### Scenario: API self-purchase error message

- **WHEN** the client receives `COMMERCE-409-SELF-PURCHASE`
- **THEN** it displays a message indicating the user cannot buy their own product
