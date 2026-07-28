## ADDED Requirements

### Requirement: Cleaned posts retain product tag commerce identifiers
The clean pipeline SHALL include product-tag snapshots on cleaned posts output when present on source Mongo documents, preserving commerce identifiers needed for offline `cross_domain_product_score` (at least `categoryId`/`category_id` and `shopId`/`shop_id` when available, plus product id when available).

#### Scenario: Product tags survive clean
- **WHEN** a Mongo post has `productTags` entries with `categoryId` and `shopId`
- **THEN** the cleaned posts CSV/dataset extract includes a `product_tags` (or equivalent) field from which build-dataset can parse those identifiers

#### Scenario: Missing product tags remain valid
- **WHEN** a post has no product tags
- **THEN** the post is still kept if it otherwise passes clean validation
- **AND** the product-tags field is empty or omitted without failing the clean job
