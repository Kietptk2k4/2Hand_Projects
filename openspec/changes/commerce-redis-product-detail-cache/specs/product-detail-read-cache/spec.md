## ADDED Requirements

### Requirement: Buyer product detail is cacheable in Redis

When a client requests a buyer-visible product detail and Redis contains a valid cached entry for that product id, the system SHALL return the cached detail without reassembling from the database. On cache miss, the system SHALL load from the database and SHALL store the result in Redis with a short TTL.

#### Scenario: Cache hit returns stored detail

- **WHEN** a client requests product detail for product `P`
- **AND** Redis contains key `commerce:product:detail:P` with a non-expired value
- **THEN** the system returns that cached detail
- **AND** does not re-run the full product-detail database assemble for that request

#### Scenario: Cache miss loads DB and populates cache

- **WHEN** a client requests product detail for product `P`
- **AND** Redis has no valid entry for `P`
- **AND** the product is buyer-visible in the database
- **THEN** the system returns the database-assembled detail
- **AND** stores it under `commerce:product:detail:P` with TTL of at most 60 seconds (configurable default)

#### Scenario: Redis failure still serves product detail

- **WHEN** Redis is unavailable during product detail read or write-cache
- **THEN** the system loads and returns detail from the database when the product is visible
- **AND** does not fail the request solely because of Redis
- **AND** logs a warning

### Requirement: Product detail cache is invalidated after mutating writes

After a successful commit of a write that changes buyer-visible product detail fields for a product, the system SHALL delete that product’s detail cache key so the next read reassembles from the database.

#### Scenario: Price update invalidates cache

- **WHEN** a seller successfully updates the price of product `P`
- **THEN** after commit the system deletes `commerce:product:detail:P`

#### Scenario: Inventory update or reserve/release invalidates cache

- **WHEN** stock displayed on product detail changes for product `P` via seller inventory update or inventory reserve/release
- **THEN** after commit the system deletes `commerce:product:detail:P`

#### Scenario: Lifecycle or content update invalidates cache

- **WHEN** product `P` is successfully updated, media/attributes updated, published, paused, archived, removed, or restored
- **THEN** after commit the system deletes `commerce:product:detail:P`

#### Scenario: Shop vacation update invalidates affected product caches

- **WHEN** a shop vacation setting that is embedded in product detail is successfully updated for shop `S`
- **THEN** after commit the system deletes detail cache keys for products belonging to shop `S` that may have been cached

### Requirement: Purchase paths remain authoritative without relying on detail cache

Add-to-cart and checkout purchasability checks SHALL continue to load authoritative product/purchase state from the database (or existing purchase read models), and SHALL NOT use the product-detail Redis cache as the source of truth for stock or price at purchase time.

#### Scenario: Add to cart does not trust detail cache

- **WHEN** a buyer adds a product to cart
- **THEN** purchasability and stock checks use the existing purchase/inventory read path
- **AND** do not read `commerce:product:detail:{productId}` as the authority for those checks
